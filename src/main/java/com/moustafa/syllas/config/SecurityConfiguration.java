package com.moustafa.syllas.config;

import com.moustafa.syllas.security.AuthoritiesConstants;
import com.moustafa.syllas.security.jwt.JWTFilter;
import com.moustafa.syllas.security.jwt.TokenProvider;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.util.StringUtils;
import org.zalando.problem.spring.webflux.advice.security.SecurityProblemSupport;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration {

    private final TokenProvider tokenProvider;

    private final SecurityProblemSupport problemSupport;

    private final ReactiveUserDetailsService userDetailsService;

    public SecurityConfiguration(TokenProvider tokenProvider, SecurityProblemSupport problemSupport,ReactiveUserDetailsService userDetailsService ) {
        this.tokenProvider = tokenProvider;
        this.problemSupport = problemSupport;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService(SecurityProperties properties) {
        SecurityProperties.User user = properties.getUser();
        UserDetails userDetails = User
            .withUsername(user.getName())
            .password("{noop}" + user.getPassword())
            .roles(StringUtils.toStringArray(user.getRoles()))
            .build();
        return new MapReactiveUserDetailsService(userDetails);
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder());
        return authenticationManager;
    }

/*    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(ReactiveUserDetailsService userDetailsService) {
        return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
    }*/

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // @formatter:off
        http
            .securityMatcher(new NegatedServerWebExchangeMatcher(new OrServerWebExchangeMatcher(
                pathMatchers("/app/**", "/i18n/**", "/content/**", "/swagger-ui/**", "/test/**"),
                pathMatchers(HttpMethod.OPTIONS, "/**")
            )))
            .csrf()
                .disable()
            .addFilterAt(new JWTFilter(tokenProvider), SecurityWebFiltersOrder.HTTP_BASIC)
            .exceptionHandling()
                .accessDeniedHandler(problemSupport)
                .authenticationEntryPoint(problemSupport)
        .and()
            .headers()
                .contentSecurityPolicy("default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:")
            .and()
                .referrerPolicy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            .and()
                .featurePolicy("geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
            .and()
                .frameOptions().disable()
        .and()
            .requestCache()
            .requestCache(NoOpServerRequestCache.getInstance())
        .and()
            .authorizeExchange()
            .pathMatchers("/api/auth-info").permitAll()
            .pathMatchers("/api/**").authenticated()
            .pathMatchers("/management/health").permitAll()
            .pathMatchers("/management/info").permitAll()
            .pathMatchers("/management/prometheus").permitAll()
            .pathMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN);
        // @formatter:on
        return http.build();
    }
}

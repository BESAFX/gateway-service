package com.besafx.cloud.gatewayserver.config;

import com.besafx.cloud.common.jwt.JwtConfig;
import com.besafx.cloud.common.jwt.JwtManager;
import com.besafx.cloud.common.jwt.JwtTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private JwtManager jwtManager;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.cors();

        http.httpBasic().disable();

        http.csrf().disable();

        http.logout().disable();

        http.formLogin().disable();

        // make sure we use stateless session; session won't be used to store user's state.
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.anonymous();

        // handle an authorized attempts
        http.exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        http.addFilterAfter(new JwtTokenFilter(authenticationManager(), jwtConfig, jwtManager), UsernamePasswordAuthenticationFilter.class);

        http.authorizeRequests()
                .antMatchers("/actuator/**").permitAll()

                .antMatchers("/auth-service/**").permitAll()
                .antMatchers("/contact-service/**").permitAll()
                .antMatchers("/manage-service/**").permitAll()
                .antMatchers("/warehouse-service/**").permitAll()
                .antMatchers("/purchase-service/**").permitAll()
                .antMatchers("/sale-service/**").permitAll()
                .antMatchers("/employee-service/**").permitAll()

                .anyRequest().authenticated();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Collections.singletonList("x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @RefreshScope
    public JwtConfig jwtConfig() {
        return new JwtConfig();
    }
}
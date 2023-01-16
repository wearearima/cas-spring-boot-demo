package com.example.springbootclient.config;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private SingleSignOutFilter singleSignOutFilter;
    private LogoutFilter logoutFilter;
    private ServiceProperties serviceProperties;

    @Autowired
    public SecurityConfig(SingleSignOutFilter singleSignOutFilter, LogoutFilter logoutFilter,
                             ServiceProperties serviceProperties) {
        this.logoutFilter = logoutFilter;
        this.singleSignOutFilter = singleSignOutFilter;
        this.serviceProperties = serviceProperties;
    }

    @Bean
    public UserDetailsService userDetailsService(BCryptPasswordEncoder bCryptPasswordEncoder) { 
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("user")
            .password(bCryptPasswordEncoder.encode("userPass"))
            .roles("USER")
            .build());
        manager.createUser(User.withUsername("admin")
            .password(bCryptPasswordEncoder.encode("adminPass"))
            .roles("USER", "ADMIN")
            .build());
    return manager;
}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
          .antMatchers( "/secured").authenticated()
          .and()
          .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
          .and()
          .addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class)
          .addFilterBefore(logoutFilter, LogoutFilter.class)
          .csrf().ignoringAntMatchers("/exit/cas");

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder, UserDetailsService userDetailService) 
        throws Exception {
            return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailService)
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();
    }

    public AuthenticationEntryPoint authenticationEntryPoint() {
        CasAuthenticationEntryPoint entryPoint = new CasAuthenticationEntryPoint();
        entryPoint.setLoginUrl("https://localhost:8443/cas/login");
        entryPoint.setServiceProperties(serviceProperties);
        return entryPoint;
    }
}

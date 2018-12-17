package net.remgant.familyclock.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final static Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final static String REALM = "REMGANT_REALM";

    @Value("${rest.user:}")
    private String restUser;
    @Value("${rest.password:}")
    private String restPassword;
    private final DataSource authDataSource;

    @Autowired
    public SecurityConfiguration(DataSource authDataSource) {
        this.authDataSource = authDataSource;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(restUser).password(restPassword).roles("USER");
        auth.jdbcAuthentication().dataSource(authDataSource).passwordEncoder(passwordEncoder());
    }

    @Configuration
    @Order(1)
    public static class TrackingSecurityConfigurer extends WebSecurityConfigurerAdapter {
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            httpSecurity
                    .csrf().disable()
                    .antMatcher("/tracking")
                    .authorizeRequests()
                    .anyRequest()
                    .hasRole("USER")
                    .and()
                    .httpBasic()
                    .authenticationEntryPoint(basciAuthEntryPoint());
        }
    }

    @Configuration
    @Order(2)
    public static class StatusSecurityConfigurer extends WebSecurityConfigurerAdapter {
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            httpSecurity
                    .csrf().disable()
                    .antMatcher("/status")
                    .authorizeRequests()
                    .anyRequest()
                    .hasRole("ADMIN")
                    .and()
                    .httpBasic()
                    .authenticationEntryPoint(basciAuthEntryPoint());
        }
    }

    @Configuration
    @Order(3)
    public static class DefaultSecurityConfigurer extends WebSecurityConfigurerAdapter {
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            httpSecurity
                    .csrf().disable()
                    .authorizeRequests().anyRequest().permitAll();
        }

    }

    @Bean
    public static BasicAuthenticationEntryPoint basciAuthEntryPoint() {
        return new BasicAuthenticationEntryPoint() {
            @Override
            public void afterPropertiesSet() throws Exception {
                setRealmName(REALM);
                super.afterPropertiesSet();
            }

            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.addHeader("WWW-Authenticate", "Basic realm=" + REALM);
                log.warn("Authentication failure: {}", authException.getMessage());
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

package com.robogames.RoboCupMS.Security;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Business.Security.TokenAuthorization;
import com.robogames.RoboCupMS.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Konfigurace zabazepeceni serveru
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

        @Value("${APP_FRONTEND_URL:http://localhost:3000}")
        private String frontendUrl;

        @Autowired
        private Environment environment;

        private static final String[] NOT_SECURED = new String[] {
                        // sekce prihlasovani a registrace
                        //GlobalConfig.AUTH_PREFIX + "/login",
                        //GlobalConfig.AUTH_PREFIX + "/register",
                        //GlobalConfig.AUTH_PREFIX + "/oAuth2",
                        GlobalConfig.API_PREFIX + "/validate",

                        // verejnosti umozni zobrazovat vytvorene souteze a registrovane tymy
                        GlobalConfig.API_PREFIX + "/competition/all",
                        GlobalConfig.API_PREFIX + "/competition/allRegistrations",

                        // verejnosti umozni zobrazovat informace of vytvorenych disciplinach
                        GlobalConfig.API_PREFIX + "/discipline/all",
                        GlobalConfig.API_PREFIX + "/discipline/all/visible",
                        GlobalConfig.API_PREFIX + "/discipline/get",

                        // verejnosti umozni zobrazovat informace o robotech
                        GlobalConfig.API_PREFIX + "/robot/allConfirmed",
                        GlobalConfig.API_PREFIX + "/robot/get",

                        // verejnisti umozni zobrazovat informace o hristich
                        GlobalConfig.API_PREFIX + "/playground/all",
                        GlobalConfig.API_PREFIX + "/playground/get",
                        GlobalConfig.API_PREFIX + "/playground/getMatches",

                        // vysledky souteze (dostupne i pro verejnost)
                        GlobalConfig.MODULE_PREFIX + "/competitionEvaluation/scoreOfAll",
                        GlobalConfig.MODULE_PREFIX + "/competitionEvaluation/scoreOfTeam",
                        GlobalConfig.MODULE_PREFIX + "/competitionEvaluation/scoreOfRobot",
                        GlobalConfig.MODULE_PREFIX + "/competitionEvaluation/getOrder",

                        // vysledky souteze (dostupne i pro verejnost)
                        GlobalConfig.MODULE_PREFIX + "/orderManagement/currentMatches",
                        GlobalConfig.MODULE_PREFIX + "/orderManagement/upcommingMatches",
                        GlobalConfig.MODULE_PREFIX + "/orderManagement/scheduledMatches"
        };

        @Autowired
        private UserRepository repository;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
                // token authorization filter
                TokenAuthorization tokenAuthorizationFilter = new TokenAuthorization(
                                GlobalConfig.HEADER_FIELD_TOKEN,
                                repository, NOT_SECURED);

                // povoli Pre-flight Request
                http.cors();

                // Check if we're running in local profile (without SSL)
                boolean isLocalProfile = environment != null && 
                        java.util.Arrays.asList(environment.getActiveProfiles()).contains("local");

                // konfigurace zabezpeceni
                if (!isLocalProfile) {
                        // Require HTTPS for production
                        http.csrf().disable()
                                        .requiresChannel(channel -> channel.anyRequest().requiresSecure())
                                        .addFilterAfter(tokenAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                                        .authorizeRequests()
                                        .antMatchers(NOT_SECURED)
                                        .permitAll()
                                        .anyRequest().authenticated();
                } else {
                        // Allow HTTP for local development
                        http.csrf().disable()
                                        .addFilterAfter(tokenAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                                        .authorizeRequests()
                                        .antMatchers(NOT_SECURED)
                                        .permitAll()
                                        .anyRequest().authenticated();
                }
        }

}

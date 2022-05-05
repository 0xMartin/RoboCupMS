package com.robogames.RoboCupMS.Security;

import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

        private static final String[] NOT_SECURED = new String[] {
                        // sekce prihlasovani a registrace
                        GlobalConfig.AUTH_PREFIX + "/login",
                        GlobalConfig.AUTH_PREFIX + "/register",

                        // verejnosti umozni zobrazovat vytvorene souteze a registrovane tymy
                        GlobalConfig.API_PREFIX + "/competition/all",
                        GlobalConfig.API_PREFIX + "/competition/allRegistrations",

                        // verejnosti umozni zobrazovat informace of vytvorenych disciplinach
                        GlobalConfig.API_PREFIX + "/discipline/all",
                        GlobalConfig.API_PREFIX + "/discipline/get",

                        // verejnosti umozni zobrazovat informace o robotech
                        GlobalConfig.API_PREFIX + "/robot/all",
                        GlobalConfig.API_PREFIX + "/robot/get",

                        // verejnisti umozni zobrazovat informace o hristich
                        GlobalConfig.API_PREFIX + "/playground/all",
                        GlobalConfig.API_PREFIX + "/playground/get",
                        GlobalConfig.API_PREFIX + "/playground/getMatches",

                        // sekce vysledku souteze (dostupne i pro verejnost)
                        GlobalConfig.MODULE_PREFIX + "/competitionEvaluation/scoreOfAll",
                        GlobalConfig.MODULE_PREFIX + "/competitionEvaluation/scoreOfTeam",
                        GlobalConfig.MODULE_PREFIX + "/competitionEvaluation/scoreOfRobot",
                        GlobalConfig.MODULE_PREFIX + "/competitionEvaluation/getOrder"
        };

        @Autowired
        private UserRepository repository;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
                http.csrf().disable()
                                .addFilterAfter(
                                                new TokenAuthorizationFilter(GlobalConfig.HEADER_FIELD_TOKEN,
                                                                repository, NOT_SECURED),
                                                UsernamePasswordAuthenticationFilter.class)
                                .authorizeRequests()
                                .antMatchers(NOT_SECURED)
                                .permitAll()
                                .anyRequest().authenticated();
        }

}
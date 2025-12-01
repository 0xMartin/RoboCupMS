package com.robogames.RoboCupMS;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class GlobalConfig {

    // -CONFIG-START------------------------------------------------------------------

    /**
     * Prefix pro vsechny API serveru
     */
    public static final transient String API_PREFIX = "/api";

    /**
     * Module prefix
     */
    public static final transient String MODULE_PREFIX = "/module";

    /**
     * Nazev promenne v headeru requestu pro pristupovy token
     */
    public static String HEADER_FIELD_TOKEN = "Authorization";

    /**
     * Doba platnosti pristupoveho tokenu [min]
     */
    public static int TOKEN_VALIDITY_DURATION = 30;

    /**
     * Minimalni vek uzivatele
     */
    public static int USER_MIN_AGE = 6;

    /**
     * Maximalni vek uzivatele
     */
    public static int USER_MAX_AGE = 99;

    /**
     * Defaultni nastaveni kategorii
     * nizka vekova skupina (nastaveno do 15 let)
     * vysoka vekova skupina 
     */

    /**
     * Maximalni vekove hranice pro nizkou vekovou kategorii
     */
    public static int LOW_AGE_CATEGORY_MAX_AGE = 15;

    /**
     * Maximalni pocet registrovanych robotu v discipline na jeden tym
     */
    public static int MAX_ROBOTS_IN_DISCIPLINE = 1;

    /**
     * Maximalni pocet clenu v jednom tymu
     */
    public static int MAX_TEAM_MEMBERS = 4;

    /**
     * Enkoder hesel
     */
    public static transient PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    // -CONFIG-END--------------------------------------------------------------------

}

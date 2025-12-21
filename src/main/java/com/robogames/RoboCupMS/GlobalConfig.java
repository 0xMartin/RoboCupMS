package com.robogames.RoboCupMS;

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
     * Minimalni delka nazvu robota
     */
    public static int MIN_ROBOT_NAME_LENGTH = 2;

    /**
     * Maximalni delka nazvu robota
     */
    public static int MAX_ROBOT_NAME_LENGTH = 30;

    /**
     * Minimalni delka nazvu tymu
     */
    public static int MIN_TEAM_NAME_LENGTH = 2;

    /**
     * Maximalni delka nazvu tymu
     */
    public static int MAX_TEAM_NAME_LENGTH = 30;

    /**
     * Maximalni pocet clenu v jednom tymu
     */
    public static int MAX_TEAM_MEMBERS = 4;

    // -CONFIG-END--------------------------------------------------------------------

}

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
     * Interval (s) pro ukladani posledniho pristupu do DB
     */
    public static int TOKEN_REFRESH_SAVE_INTERVAL_SECONDS = 60;

    /**
     * Omezeni delky udaju uzivatele
     */
    public static int USER_NAME_MIN_LENGTH = 2;
    public static int USER_NAME_MAX_LENGTH = 40;
    public static int USER_SURNAME_MIN_LENGTH = 2;
    public static int USER_SURNAME_MAX_LENGTH = 60;
    public static int USER_EMAIL_MIN_LENGTH = 8;
    public static int USER_EMAIL_MAX_LENGTH = 120;

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
     * Omezeni delky nazvu robota
     */
    public static int MIN_ROBOT_NAME_LENGTH = 2;
    public static int MAX_ROBOT_NAME_LENGTH = 30;

    /**
     * Omezeni delky nazvu tymu
     */
    public static int MIN_TEAM_NAME_LENGTH = 2;
    public static int MAX_TEAM_NAME_LENGTH = 30;

    /**
     * Maximalni pocet clenu v jednom tymu
     */
    public static int MAX_TEAM_MEMBERS = 4;

    /**
     * Omezeni delky udaju ucitele
     */
    public static int MIN_TEACHER_NAME_LENGTH = 2;
    public static int MAX_TEACHER_NAME_LENGTH = 40;
    public static int MIN_TEACHER_SURNAME_LENGTH = 2;
    public static int MAX_TEACHER_SURNAME_LENGTH = 60;
    public static int MIN_TEACHER_CONTACT_LENGTH = 9;
    public static int MAX_TEACHER_CONTACT_LENGTH = 120;


    // -CONFIG-END--------------------------------------------------------------------

}

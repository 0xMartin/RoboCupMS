package com.robogames.RoboCupMS;

import java.io.FileReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Enum.EScoreAggregation;
import com.robogames.RoboCupMS.Entity.Category;
import com.robogames.RoboCupMS.Entity.Discipline;
import com.robogames.RoboCupMS.Entity.MatchState;
import com.robogames.RoboCupMS.Entity.Role;
import com.robogames.RoboCupMS.Entity.ScoreAggregation;
import com.robogames.RoboCupMS.Entity.UserRC;
import com.robogames.RoboCupMS.Repository.CategoryRepository;
import com.robogames.RoboCupMS.Repository.DisciplineRepository;
import com.robogames.RoboCupMS.Repository.MatchStateRepository;
import com.robogames.RoboCupMS.Repository.RoleRepository;
import com.robogames.RoboCupMS.Repository.ScoreAggregationRepository;
import com.robogames.RoboCupMS.Repository.UserRepository;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Zajistuje inicializaci aplikace
 */
@Configuration
public class AppInit {

    private static final Logger logger = LoggerFactory.getLogger(AppInit.class);

    /**
     * Navrati poskotovatele aplikacniho kontextu
     * 
     * @return ApplicationContextProvider
     */
    @Bean
    public static ApplicationContextProvider contextProvider() {
        return new ApplicationContextProvider();
    }

    /**
     * Nacte konfiguraci ze souboru
     */
    @Bean
    public static void loadConfigFromFile() {
        try {
            JSONParser parser = new JSONParser();
            JSONObject obj = null;

            obj = (JSONObject) parser.parse(new FileReader("config.json"));

            // nazev pristupoveho tokenu v headeru requestu
            String HEADER_FIELD_TOKEN = (String) obj.get("HEADER_FIELD_TOKEN");
            if (HEADER_FIELD_TOKEN != null) {
                GlobalConfig.HEADER_FIELD_TOKEN = HEADER_FIELD_TOKEN;
                logger.info("HEADER_FIELD_TOKEN set on: " + HEADER_FIELD_TOKEN);
            }

            // zivotnost uzivatelskeho tokenu
            Long TOKEN_VALIDITY_DURATION = (Long) obj.get("TOKEN_VALIDITY_DURATION");
            if (TOKEN_VALIDITY_DURATION != null) {
                GlobalConfig.TOKEN_VALIDITY_DURATION = (int) TOKEN_VALIDITY_DURATION.longValue();
                logger.info("TOKEN_VALIDITY_DURATION set on: " + TOKEN_VALIDITY_DURATION);
            }

            // minimalni vek uzivatele
            Long USER_MIN_AGE = (Long) obj.get("USER_MIN_AGE");
            if (USER_MIN_AGE != null) {
                GlobalConfig.USER_MIN_AGE = (int) USER_MIN_AGE.longValue();
                logger.info("USER_MIN_AGE set on: " + USER_MIN_AGE);
            }

            // maximalni vek uzivatel
            Long USER_MAX_AGE = (Long) obj.get("USER_MAX_AGE");
            if (USER_MAX_AGE != null) {
                GlobalConfig.USER_MAX_AGE = (int) USER_MAX_AGE.longValue();
                logger.info("USER_MAX_AGE set on: " + USER_MAX_AGE);
            }

            // maximalni vek pro nizsi vekovou kategorii
            Long LOW_AGE_CATEGORY_MAX_AGE = (Long) obj.get("LOW_AGE_CATEGORY_MAX_AGE");
            if (LOW_AGE_CATEGORY_MAX_AGE != null) {
                GlobalConfig.LOW_AGE_CATEGORY_MAX_AGE = (int) LOW_AGE_CATEGORY_MAX_AGE.longValue();
                logger.info("LOW_AGE_CATEGORY_MAX_AGE set on: " + LOW_AGE_CATEGORY_MAX_AGE);
            }

            // maximalni mocet robotu v discipline na jeden tym
            Long MAX_ROBOTS_IN_DISCIPLINE = (Long) obj.get("MAX_ROBOTS_IN_DISCIPLINE");
            if (MAX_ROBOTS_IN_DISCIPLINE != null) {
                GlobalConfig.MAX_ROBOTS_IN_DISCIPLINE = (int) MAX_ROBOTS_IN_DISCIPLINE.longValue();
                logger.info("MAX_ROBOTS_IN_DISCIPLINE set on: " + MAX_ROBOTS_IN_DISCIPLINE);
            }

            // maximalni mocet robotu v kategorii na jeden tym
            Long MAX_TEAM_MEMBERS = (Long) obj.get("MAX_TEAM_MEMBERS");
            if (MAX_TEAM_MEMBERS != null) {
                GlobalConfig.MAX_TEAM_MEMBERS = (int) MAX_TEAM_MEMBERS.longValue();
                logger.info("MAX_TEAM_MEMBERS set on: " + MAX_TEAM_MEMBERS);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prvni inicializace roli
     * 
     * @param repository RoleRepository
     */
    @Bean
    public ApplicationRunner initRole(RoleRepository repository) {
        if (repository.count() == 0) {
            return args -> repository.saveAll(Arrays.asList(
                    new Role(ERole.ADMIN),
                    new Role(ERole.LEADER),
                    new Role(ERole.ASSISTANT),
                    new Role(ERole.REFEREE),
                    new Role(ERole.COMPETITOR)));
        } else {
            return null;
        }
    }

    /**
     * Prvni inicializace agregacnich funkci skore (pouziva se pro automaticke
     * vyhodnoceni vysledku souteze)
     * 
     * @param repository RoleRepository
     */
    @Bean
    public ApplicationRunner initScoreAggregation(ScoreAggregationRepository repository) {
        if (repository.count() == 0) {
            return args -> repository.saveAll(Arrays.asList(
                    new ScoreAggregation(EScoreAggregation.MAX),
                    new ScoreAggregation(EScoreAggregation.MIN),
                    new ScoreAggregation(EScoreAggregation.SUM)));
        } else {
            return null;
        }
    }

    /**
     * Prvni inicializace kategorii
     * 
     * @param repository RoleRepository
     */
    @Bean
    public ApplicationRunner initCategory(CategoryRepository repository) {
        if (repository.count() == 0) {
            return args -> repository.saveAll(Arrays.asList(
                    new Category(ECategory.LOW_AGE_CATEGORY),
                    new Category(ECategory.HIGH_AGE_CATEGORY)));
        } else {
            return null;
        }
    }

    /**
     * Prvni inicializace stavu zapasu
     * 
     * @param repository RoleRepository
     */
    @Bean
    public ApplicationRunner initMatchState(MatchStateRepository repository) {
        if (repository.count() == 0) {
            return args -> repository.saveAll(Arrays.asList(
                    new MatchState(EMatchState.DONE),
                    new MatchState(EMatchState.REMATCH),
                    new MatchState(EMatchState.WAITING)));
        } else {
            return null;
        }
    }

    /**
     * Prvni inicializace uzivatelu (administratori)
     * 
     * @param repository RoleRepository
     */
    @Bean
    public ApplicationRunner initUsers(UserRepository repository) {
        if (repository.count() == 0) {
            List<ERole> admin_role = Arrays.asList(new ERole[] { ERole.ADMIN });
            return args -> repository.saveAll(Arrays.asList(
                    new UserRC(
                            "Martin",
                            "Krcma",
                            "m1_krcma@utb.cz",
                            "A12Admin34n56",
                            new GregorianCalendar(1999, Calendar.OCTOBER, 17).getTime(),
                            admin_role),
                    new UserRC(
                            "Pavel",
                            "Sevcik",
                            "p_sevcik@utb.cz",
                            "A12Admin34n56",
                            new GregorianCalendar(1999, Calendar.NOVEMBER, 12).getTime(),
                            admin_role),
                    new UserRC(
                            "Eliska",
                            "Obadalova",
                            "e_obadalova@utb.cz",
                            "A12Admin34n56",
                            new GregorianCalendar(1999, Calendar.NOVEMBER, 6).getTime(),
                            admin_role)));
        } else {
            return null;
        }
    }

    /**
     * Prvni inicializace zakladnich disciplin
     * 
     * @param repository DisciplineRepository
     */
    @Bean
    public ApplicationRunner initDisciplines(DisciplineRepository repository,
            ScoreAggregationRepository aggregationRepository) {
        if (repository.count() == 0) {
            return args -> repository.saveAll(Arrays.asList(
                    new Discipline(
                            "Robosumo",
                            "Vítězí ten robot, který svého protivníka vytlačí ven. Rožmer: 25x25cm, Hmotnost: 1kg",
                            EScoreAggregation.SUM,
                            9 * 60,
                            Discipline.NOT_LIMITED_NUMBER_OF_ROUNDS),
                    new Discipline(
                            "Mini robosumo",
                            "Vítězí ten robot, který svého protivníka vytlačí ven. Rožmer: 15x15cm, Hmotnost: 1kg",
                            EScoreAggregation.SUM,
                            9 * 60,
                            Discipline.NOT_LIMITED_NUMBER_OF_ROUNDS),
                    new Discipline(
                            "Sledování čáry",
                            "Soutěž vyhrává robot, který nejrychleji projede dráhu, realizovanou jako černá čára na bílém podkladu.",
                            EScoreAggregation.MIN,
                            3 * 60,
                            Discipline.NOT_LIMITED_NUMBER_OF_ROUNDS),
                    new Discipline(
                            "Robot uklízeč",
                            "Smyslem této disciplíny je posbírat kostky rozmístěné na soutěžní ploše a přivést je do určené oblasti.",
                            EScoreAggregation.MIN,
                            3 * 60,
                            3),
                    new Discipline(
                            "Micromouse",
                            "Robot projíždí bludiště ze startu do cíle.",
                            EScoreAggregation.MIN,
                            10 * 60,
                            Discipline.NOT_LIMITED_NUMBER_OF_ROUNDS)));
        } else {
            return null;
        }
    }

}

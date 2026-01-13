package com.robogames.RoboCupMS;

import java.io.FileReader;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;

import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Business.Enum.EScoreAggregation;
import com.robogames.RoboCupMS.Business.Enum.EScoreType;
import com.robogames.RoboCupMS.Business.Enum.ETournamentPhase;
import com.robogames.RoboCupMS.Entity.Category;
import com.robogames.RoboCupMS.Entity.Discipline;
import com.robogames.RoboCupMS.Entity.MatchState;
import com.robogames.RoboCupMS.Entity.Role;
import com.robogames.RoboCupMS.Entity.ScoreAggregation;
import com.robogames.RoboCupMS.Entity.ScoreType;
import com.robogames.RoboCupMS.Entity.TournamentPhase;
import com.robogames.RoboCupMS.Entity.UserRC;
import com.robogames.RoboCupMS.Repository.CategoryRepository;
import com.robogames.RoboCupMS.Repository.DisciplineRepository;
import com.robogames.RoboCupMS.Repository.MatchStateRepository;
import com.robogames.RoboCupMS.Repository.RoleRepository;
import com.robogames.RoboCupMS.Repository.ScoreAggregationRepository;
import com.robogames.RoboCupMS.Repository.ScoreTypeRepository;
import com.robogames.RoboCupMS.Repository.TournamentPhaseRepository;
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

    @Value("${app.super-admin.email}")
    private String adminEmail;

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

            // interval (s) pro ukladani posledniho pristupu do DB
            Long TOKEN_REFRESH_SAVE_INTERVAL_SECONDS = (Long) obj.get("TOKEN_REFRESH_SAVE_INTERVAL_SECONDS");
            if (TOKEN_REFRESH_SAVE_INTERVAL_SECONDS != null) {
                GlobalConfig.TOKEN_REFRESH_SAVE_INTERVAL_SECONDS = (int) TOKEN_REFRESH_SAVE_INTERVAL_SECONDS.longValue();
                logger.info("TOKEN_REFRESH_SAVE_INTERVAL_SECONDS set on: " + TOKEN_REFRESH_SAVE_INTERVAL_SECONDS);
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
     * Initialize match states
     * 
     * @param repository MatchStateRepository
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
     * Initialize tournament phases
     * 
     * @param repository TournamentPhaseRepository
     */
    @Bean
    public ApplicationRunner initTournamentPhase(TournamentPhaseRepository repository) {
        if (repository.count() == 0) {
            return args -> repository.saveAll(Arrays.asList(
                    new TournamentPhase(ETournamentPhase.PRELIMINARY),
                    new TournamentPhase(ETournamentPhase.QUARTERFINAL),
                    new TournamentPhase(ETournamentPhase.SEMIFINAL),
                    new TournamentPhase(ETournamentPhase.FINAL),
                    new TournamentPhase(ETournamentPhase.THIRD_PLACE)));
        } else {
            return null;
        }
    }

    /**
     * Initialize score types
     * 
     * @param repository ScoreTypeRepository
     */
    @Bean
    public ApplicationRunner initScoreType(ScoreTypeRepository repository) {
        if (repository.count() == 0) {
            return args -> repository.saveAll(Arrays.asList(
                    new ScoreType(EScoreType.TIME),
                    new ScoreType(EScoreType.SCORE)));
        } else {
            return null;
        }
    }

    /**
     * Email administrátorského účtu, který bude povýšen na roli ADMIN
     * 
     * @param userRepository UserRepository 
     * @param repository RoleRepository
     */
    @Bean
    public ApplicationRunner initAdmin(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            Optional<Role> optRole = roleRepository.findByName(ERole.ADMIN);
            Optional<UserRC> user = userRepository.findByEmail(adminEmail);
            if (user.isPresent()) {
                UserRC admin = user.get();
                if (!admin.getRoles().contains(optRole.get())) {
                    admin.getRoles().add(optRole.get());
                    userRepository.save(admin);
                    System.out.println("User " + adminEmail + " was promoted to ADMIN.");
                }
            }
        };
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
                            "Vítězí ten robot, který svého protivníka vytlačí ven. Rožmer: 20x20cm, Hmotnost: 1kg",
                            EScoreAggregation.SUM,
                            9 * 60,
                            Discipline.NOT_LIMITED_NUMBER_OF_ROUNDS),
                    new Discipline(
                            "Mini robosumo",
                            "Vítězí ten robot, který svého protivníka vytlačí ven. Rožmer: 10x10cm, Hmotnost: 500g",
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

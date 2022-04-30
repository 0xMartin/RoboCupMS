package com.robogames.RoboCupMS;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.robogames.RoboCupMS.Entity.Category;
import com.robogames.RoboCupMS.Entity.MatchState;
import com.robogames.RoboCupMS.Entity.Role;
import com.robogames.RoboCupMS.Entity.UserRC;
import com.robogames.RoboCupMS.Enum.ECategory;
import com.robogames.RoboCupMS.Enum.EMatchState;
import com.robogames.RoboCupMS.Enum.ERole;
import com.robogames.RoboCupMS.Repository.CategoryRepository;
import com.robogames.RoboCupMS.Repository.MatchStateRepository;
import com.robogames.RoboCupMS.Repository.RoleRepository;
import com.robogames.RoboCupMS.Repository.UserRepository;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppInit {

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
     * Prvni inicializace roli
     * 
     * @param repository RoleRepository
     * @return
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
     * Prvni inicializace kategorii
     * 
     * @param repository RoleRepository
     * @return
     */
    @Bean
    public ApplicationRunner initCategory(CategoryRepository repository) {
        if (repository.count() == 0) {
            return args -> repository.saveAll(Arrays.asList(
                    new Category(ECategory.ELEMENTARY_SCHOOL),
                    new Category(ECategory.HIGH_SCHOOL),
                    new Category(ECategory.UNIVERSITY),
                    new Category(ECategory.OPEN)));
        } else {
            return null;
        }
    }

    /**
     * Prvni inicializace stavu zapasu
     * 
     * @param repository RoleRepository
     * @return
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
     * @return
     */
    @Bean
    public ApplicationRunner initUsers(UserRepository repository) {
        if (repository.count() == 0) {
            List<ERole> admin_role = Arrays.asList(new ERole[] { ERole.ADMIN });
            return args -> repository.saveAll(Arrays.asList(
                    new UserRC("Martin", "Krcma", "m1_krcma@utb.cz", "Admin123", new Date(), admin_role),
                    new UserRC("Pavel", "Sevcik", "p_sevcik@utb.cz", "Admin123", new Date(), admin_role),
                    new UserRC("Eliska", "Obadalova", "e_obadalova@utb.cz", "Admin123", new Date(), admin_role)));
        } else {
            return null;
        }
    }

}

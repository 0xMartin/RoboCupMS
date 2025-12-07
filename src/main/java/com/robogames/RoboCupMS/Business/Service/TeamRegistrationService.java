package com.robogames.RoboCupMS.Business.Service;

import java.util.List;
import java.util.Optional;

import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Object.TeamRegistrationObj;
import com.robogames.RoboCupMS.Entity.Category;
import com.robogames.RoboCupMS.Entity.Competition;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.Team;
import com.robogames.RoboCupMS.Entity.TeamRegistration;
import com.robogames.RoboCupMS.Entity.UserRC;
import com.robogames.RoboCupMS.Repository.CategoryRepository;
import com.robogames.RoboCupMS.Repository.CompetitionRepository;
import com.robogames.RoboCupMS.Repository.TeamRegistrationRepository;
import com.robogames.RoboCupMS.Repository.TeamRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Zajistuje registraci tymu do souteze
 */
@Service
public class TeamRegistrationService {

    @Autowired
    private TeamRegistrationRepository registrationRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Registruje tym do souteze (registrovat muze pouze vedouci tymu!!!!!)
     * 
     * @param teamRegistrationObj Parametry nove registrace tymu
     * @throws Exception
     */
    public void register(TeamRegistrationObj teamRegistrationObj) throws Exception {
        UserRC leader = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // overi zda uzivatel je vedoucim nejakeho tymu
        Optional<Team> t = this.teamRepository.findAllByLeader(leader).stream().findFirst();
        if (!t.isPresent()) {
            throw new Exception("failure, you are not the leader of any existing team");
        }

        // overi zda rocnik souteze, do ktereho se hlasi existuje
        Optional<Competition> c = competitionRepository.findByYear(teamRegistrationObj.getYear());
        if (!c.isPresent()) {
            throw new Exception(String.format("failure, compatition [%d] not exists", teamRegistrationObj.getYear()));
        }

        // overi zda soutez jiz nezacala (registrace je mozna jen pokud soutez jeste
        // nezacala)
        if (c.get().getStarted()) {
            throw new Exception(String.format("failure, competition has already begin", teamRegistrationObj.getYear()));
        }

        // overi zda tym jiz neni prihlasen do tohoto rocniku
        List<TeamRegistration> registrations = t.get().getRegistrations();
        if (registrations.stream().anyMatch((r) -> (r.getCompatitionYear() == c.get().getYear()))) {
            throw new Exception("failure, team is already registred in this year of compatition");
        }

        // urci kategorii tymu
        ECategory cat_name = t.get().determinateCategory();
        Optional<Category> cat = this.categoryRepository.findByName(cat_name);
        if (!cat.isPresent()) {
            throw new Exception("failure, category not exists");
        }

        // registruje tym do souteze
        TeamRegistration r = new TeamRegistration(
                t.get(),
                c.get(),
                cat.get());

        this.registrationRepository.save(r);
    }

    /**
     * Zrusi registraci tymu
     * 
     * @param year Rocni souteze
     * @throws Exception
     */
    public void unregister(int year) throws Exception {
        UserRC leader = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // overi zda uzivatel je vedoucim nejakeho tymu
        Optional<Team> t = this.teamRepository.findAllByLeader(leader).stream().findFirst();
        if (!t.isPresent()) {
            throw new Exception("failure, you are not the leader of any existing team");
        }

        // overi zda rocnik souteze existuje
        Optional<Competition> c = competitionRepository.findByYear(year);
        if (!c.isPresent()) {
            throw new Exception(String.format("failure, compatition [%d] not exists", year));
        }

        // overi zda soutez jiz nezacala (registrace je mozna jen pokud soutez jeste
        // nezacala)
        if (c.get().getStarted()) {
            throw new Exception("failure, competition has already begin");
        }

        // najde registraci tymu v seznamu registraci daneho tymu
        List<TeamRegistration> registrations = t.get().getRegistrations();
        Optional<TeamRegistration> registration = registrations.stream().filter(r -> (r.getCompatitionYear() == year))
                .findFirst();
        if (!registration.isPresent()) {
            throw new Exception("failure, team registration not exists");
        }

        // overi zda jiz tym nema potvrzen registrace robotu
        for (Robot r : registration.get().getRobots()) {
            if (r.getConfirmed()) {
                throw new Exception("failure, team have a robot that is already confirmed");
            }
        }

        // odstrani registraci
        this.registrationRepository.delete(registration.get());
    }

    /**
     * Navrati vsechny registrace tymu, ve kterem se uzivatel nachazi (vsehny
     * rocniky, kterych se ucastnil)
     * 
     * @return Seznam vsech registraci
     * @throws Exception
     */
    public List<TeamRegistration> getAll() throws Exception {
        UserRC user = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // id tymu, ve kterem se uzivatel nachazi
        long team_id = user.getTeamID();
        if (team_id == Team.NOT_IN_TEAM) {
            throw new Exception("failure, you are not a member of any team");
        }

        // najde tym v datavazi
        Optional<Team> t = this.teamRepository.findById(team_id);
        if (!t.isPresent()) {
            throw new Exception("failure, team not exists");
        }

        // navrati vsechny registrace
        return t.get().getRegistrations();
    }

    /**
     * Zmeni kategorii tymu. Jiz neni nijak omezovano vekem a tak je mozne zvolit
     * libovolnou.
     * 
     * @param id       ID tymu
     * @param year     Rocnik souteze
     * @param category Nova kategorie, ve ktere bude tym soutezit
     * @throws Exception
     */
    public void changeCategory(long id, int year, ECategory category) throws Exception {
        // overi zda tym existuje
        Optional<Team> t = this.teamRepository.findById(id);
        if (!t.isPresent()) {
            throw new Exception(String.format("failure, team with ID [%d] not exists", id));
        }

        // overi zda kategorie existuje
        Optional<Category> cat = this.categoryRepository.findByName(category);
        if (!cat.isPresent()) {
            throw new Exception("failure, category not exists");
        }

        // najde registraci tymu pro dany rocnik
        Optional<TeamRegistration> reg = t.get().getRegistrations().stream()
                .filter((r) -> (r.getCompatitionYear() == year)).findFirst();
        if (!reg.isPresent()) {
            throw new Exception(
                    String.format("failure, team with ID [%d] is not registered for the year [%d]", id, year));
        }

        // overi zda soutez jiz nezacala (registrace je mozna jen pokud soutez jeste
        // nezacala)
        if (reg.get().getCompatition().getStarted()) {
            throw new Exception("failure, competition has already begun");
        }

        // provede zmeny
        reg.get().setCategory(cat.get());
        this.registrationRepository.save(reg.get());
    }

    /**
     * Slouci dve ruzne kategorie dohromady. Vybere se jedna kategorie a vsichni,
     * kteri jsou v
     * ni registrovani se pridaji k jine zvolene kategorii.
     * 
     * @param year        Rocnik souteze
     * @param category    Kategorie tymu, ktere se budou presouvat do jine
     * @param newCategory Kategorie, do ktere se presunou vsechny registrovane tymy
     *                    z jejich aktualni kategorie
     * @throws Exception
     */
    public void joinCategory(int year, ECategory category, ECategory newCategory) throws Exception {
        // overi zda kategorie existuje
        if (!this.categoryRepository.findByName(category).isPresent()) {
            throw new Exception("failure, category (param: category) not exists");
        }

        // overi zda kategorie existuje
        Optional<Category> catTo = this.categoryRepository.findByName(newCategory);
        if (!catTo.isPresent()) {
            throw new Exception("failure, category (param: newCategory) not exists");
        }

        // najde konkretni rocnik souteze
        Optional<Competition> competition = this.competitionRepository.findByYear(year);
        if (!competition.isPresent()) {
            throw new Exception(String.format("failure, compatition [%d] not exists", year));
        }

        // overi zda soutez jiz nezacala (registrace je mozna jen pokud soutez jeste
        // nezacala)
        if (competition.get().getStarted()) {
            throw new Exception("failure, competition has already begun");
        }

        // provede zmeny (slouceni kategorii)
        List<TeamRegistration> registrations = competition.get().getRegistrations();
        registrations.forEach((reg) -> {
            if (reg.getCategory() == category) {
                reg.setCategory(catTo.get());
                this.registrationRepository.save(reg);
            }
        });
    }

}

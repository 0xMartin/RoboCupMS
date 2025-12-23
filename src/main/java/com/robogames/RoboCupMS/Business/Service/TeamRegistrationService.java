package com.robogames.RoboCupMS.Business.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.robogames.RoboCupMS.GlobalConfig;
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
     * Overi platnost udaju o uciteli pro registraci tymu v pripade nizke vekove
     * kategorie. Pokud jsou udaje platne, ulozi je do registrace
     * 
     * @param teamRegistrationObj Parametry nove registrace tymu
     * @param reg                 Registrace tymu
     * @throws Exception
     */
    private void validateTeacherInfo(TeamRegistrationObj teamRegistrationObj, TeamRegistration reg) throws Exception {
        String name = teamRegistrationObj.getTeacherName();
        String surname = teamRegistrationObj.getTeacherSurname();
        String contact = teamRegistrationObj.getTeacherContact();

        if (name == null || name.isEmpty() || surname == null || surname.isEmpty() || contact == null
                || contact.isEmpty()) {
            throw new Exception("failure, teacher data must be provided for low age category");
        }

        // overeni delky udaju
        if (name.length() < GlobalConfig.MIN_TEACHER_NAME_LENGTH
                || name.length() > GlobalConfig.MAX_TEACHER_NAME_LENGTH) {
            throw new Exception("failure, teacher name length is invalid");
        }
        if (surname.length() < GlobalConfig.MIN_TEACHER_SURNAME_LENGTH
                || surname.length() > GlobalConfig.MAX_TEACHER_SURNAME_LENGTH) {
            throw new Exception("failure, teacher surname length is invalid");
        }
        if (contact.length() < GlobalConfig.MIN_TEACHER_CONTACT_LENGTH
                || contact.length() > GlobalConfig.MAX_TEACHER_CONTACT_LENGTH) {
            throw new Exception("failure, teacher contact length is invalid");
        }

        // overeni formatu kontaktu (e-mail nebo telefon)
        Pattern pattern = Pattern
                .compile("^(?:[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|\\d{9}|\\d{3} \\d{3} \\d{3})$");
        if (!pattern.matcher(contact).matches()) {
            throw new Exception("failure, contact is invalid");
        }

        // ulozi udaje o uciteli
        reg.setTeacherName(teamRegistrationObj.getTeacherName());
        reg.setTeacherSurname(teamRegistrationObj.getTeacherSurname());
        reg.setTeacherContact(teamRegistrationObj.getTeacherContact());
    }

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
        if (registrations.stream().anyMatch((r) -> (r.getCompetitionYear() == c.get().getYear()))) {
            throw new Exception("failure, team is already registred in this year of competition");
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

        // pokud je katagorie LOW_AGE_CATEGORY, overi zda byli zadany i udaje o ucitele
        if (cat_name == ECategory.LOW_AGE_CATEGORY) {
            validateTeacherInfo(teamRegistrationObj, r);
        }

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
        Optional<TeamRegistration> registration = registrations.stream().filter(r -> (r.getCompetitionYear() == year))
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
                .filter((r) -> (r.getCompetitionYear() == year)).findFirst();
        if (!reg.isPresent()) {
            throw new Exception(
                    String.format("failure, team with ID [%d] is not registered for the year [%d]", id, year));
        }

        // overi zda soutez jiz nezacala (registrace je mozna jen pokud soutez jeste
        // nezacala)
        if (reg.get().getCompetition().getStarted()) {
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

    /**
     * Ziska registraci tymu pro dany rocnik souteze
     * 
     * @param year Rocnik souteze
     * @return Registrace tymu
     * @throws Exception
     */
    public TeamRegistration getRegistration(int year) throws Exception {
        UserRC user = (UserRC) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // id tymu, ve kterem se uzivatel nachazi
        long team_id = user.getTeamID();
        if (team_id == Team.NOT_IN_TEAM) {
            throw new Exception("failure, you are not a member of any team");
        }

        // najde tym v databazi
        Optional<Team> t = this.teamRepository.findById(team_id);
        if (!t.isPresent()) {
            throw new Exception("failure, team not exists");
        }

        // najde registraci tymu pro dany rocnik souteze
        Optional<TeamRegistration> registration = t.get().getRegistrations().stream()
                .filter((r) -> (r.getCompetitionYear() == year)).findFirst();
        if (!registration.isPresent()) {
            throw new Exception(
                    String.format("failure, team registration not exists for year [%d]", year));
        }

        return registration.get();
    }

    /**
     * Aktualizuje informace o uciteli pro registraci tymu v danem rocniku
     * 
     * @param year                Rocnik souteze
     * @param teamRegistrationObj Nove udaje o uciteli
     * @throws Exception
     */
    public void updateTeacherInfo(int year, TeamRegistrationObj teamRegistrationObj) throws Exception {
        // ziska registraci tymu pro dany rocnik
        TeamRegistration registration = this.getRegistration(year);

        // overi zda soutez jiz nezacala
        if (registration.getCompetition().getStarted()) {
            throw new Exception("failure, competition has already begun");
        }

        // validace udaju o uciteli (pokud je LOW_AGE_CATEGORY)
        if (registration.getCategory() == ECategory.LOW_AGE_CATEGORY) {
            validateTeacherInfo(teamRegistrationObj, registration);
        }

        this.registrationRepository.save(registration);
    }

}

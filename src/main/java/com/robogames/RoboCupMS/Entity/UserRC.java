package com.robogames.RoboCupMS.Entity;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.CascadeType; 
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.robogames.RoboCupMS.AppInit;
import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Business.Enum.ERole;
import com.robogames.RoboCupMS.Repository.RoleRepository;

/**
 * Uzivatel (RoboCup)
 */
@Entity(name = "user")
public class UserRC {

    /**
     * ID uzivatele
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Jmeno uzivatele
     */
    @Column(name = "name", length = 40, nullable = false, unique = false)
    private String name;

    /**
     * Prijmeni uzivatele
     */
    @Column(name = "surname", length = 60, nullable = false, unique = false)
    private String surname;

    /**
     * Email uzivatele
     */
    @Column(name = "email", length = 120, nullable = false, unique = true)
    private String email;

    /**
     * Datum narozeni uzivatele
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "birthDate", nullable = false, unique = false)
    private Date birthDate;

    /**
     * Role uzivatele
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    /**
     * Tym, ve kterem se uzivatel nachazi
     */
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    /**
     * Pristupovy token uzivatele
     */
    @Column(name = "token", nullable = true, unique = true)
    private String token;

    /**
     * Keycloak refresh token pro odhlaseni z Keycloaku
     */
    @Column(name = "keycloak_refresh_token", nullable = true, unique = false, length = 4096)
    private String keycloakRefreshToken;

    /**
     * Cas posledniho pristupu uzivatele na server (pouziva se pro zneplatneni
     * pristupoveho tokenu po uplinuti prednastaveneho casu)
     */
    @Column(name = "last_access_time", nullable = true, unique = false)
    private Date lastAccessTime;

    /**
     * Vytvori noveho uzivatele robosouteze
     */
    public UserRC() {
    }

    /**
     * Vytvori noveho uzivatele robosouteze
     * 
     * @param _name      Jmeno uzivatele
     * @param _surname   Prijmeni uzivatele
     * @param _email     Email uzivatele
     * @param _birthDate Datum narozeni
     * @param _role      Vsechny role uzivatele (enum)
     */
    public UserRC(String _name, String _surname, String _email, Date _birthDate, List<ERole> _roles) {
        this.name = _name;
        this.surname = _surname;
        this.email = _email;
        this.setBirthDate(_birthDate);
        RoleRepository roleRepository = (RoleRepository) AppInit.contextProvider().getApplicationContext()
                .getBean("roleRepository");
        _roles.stream().forEach(_role -> {
            Optional<Role> opt = roleRepository.findByName(_role);
            if (opt.isPresent())
                this.roles.add(opt.get());
        });
    }

    /**
     * Navrati ID uzivatele
     * 
     * @return ID
     */
    public long getID() {
        return this.id;
    }

    /**
     * Navrati jmeno uzivatele
     * 
     * @return Jmeno uzivatele
     */
    public String getName() {
        return this.name;
    }

    /**
     * Nastavi nove jmeno uzivatele
     * 
     * @param _name Nove jmeno
     */
    public void setName(String _name) {
        this.name = _name;
    }

    /**
     * Navrati prijmeni uzivatele
     * 
     * @return Prijmeni uzivatele
     */
    public String getSurname() {
        return this.surname;
    }

    /**
     * Nastavi nove prijmeni uzivatele
     * 
     * @param _surname Nove prijmeni
     */
    public void setSurname(String _surname) {
        this.surname = _surname;
    }

    /**
     * Navrati email uzivatele
     * 
     * @return Email
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Nastavi novy email uzivatele
     * 
     * @param _email Novy email
     */
    public void setEmail(String _email) {
        this.email = _email;
    }

    /**
     * Navrati cas posledniho pristupu
     * 
     * @return
     */
    @JsonIgnore
    public Date getLastAccessTime() {
        return this.lastAccessTime;
    }

    /**
     * Nastavi cas posledniho pristupu uzivatele na server
     * 
     * @param _time Cas
     */
    public void setLastAccessTime(Date _time) {
        this.lastAccessTime = _time;
    }

    /**
     * Navrati Keycloak refresh token
     * 
     * @return Keycloak refresh token
     */
    @JsonIgnore
    public String getKeycloakRefreshToken() {
        return this.keycloakRefreshToken;
    }

    /**
     * Nastavi Keycloak refresh token
     * 
     * @param _token Keycloak refresh token
     */
    public void setKeycloakRefreshToken(String _token) {
        this.keycloakRefreshToken = _token;
    }

    /**
     * Navrati vek uzivatele
     * 
     * @return Vek
     */
    @JsonIgnore
    public int getAge() {
        LocalDate currentDate = LocalDate.now();
        if ((this.birthDate != null) && (currentDate != null)) {
            LocalDate bd = ((java.sql.Date) birthDate).toLocalDate();
            return Period.between(bd, currentDate).getYears();
        } else {
            return 0;
        }
    }

    /**
     * Navrati datum narozeni
     * 
     * @return Datum narozeni
     */
    public Date getBirthDate() {
        return this.birthDate;
    }

    /**
     * Nastavi datum narozeni
     * 
     * @param _age Vek uzivatele
     */
    public boolean setBirthDate(Date _birthDate) {
        // z datumu narozeni vypocit vek uzivatele
        LocalDate currentDate = LocalDate.now();
        LocalDate bd = _birthDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        int age = Period.between(bd, currentDate).getYears();

        // vek musi splnovat omezeni
        if (age >= GlobalConfig.USER_MIN_AGE &&
                age <= GlobalConfig.USER_MAX_AGE) {
            this.birthDate = _birthDate;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Navratit seznam Roli uzivatele
     * 
     * @return Role
     */
    public Set<Role> getRoles() {
        return this.roles;
    }

    /**
     * Nastavi seznam roli uzivatele
     * 
     * @param _role Role
     */
    public void setRoles(Set<Role> _roles) {
        this.roles = _roles;
    }

    /**
     * Navrati tym, ve kterem se uzivatel nachazi. Pokud neni v zadnem tymu navrati
     * Team.NOT_IN_TEAM
     * 
     * @return ID tymu
     */
    public long getTeamID() {
        if (this.team == null) {
            return Team.NOT_IN_TEAM;
        } else {
            return this.team.getID();
        }
    }

    /**
     * Navrati tym, ve kterem se uzivatel nachazi
     * 
     * @return Tym
     */
    @JsonIgnore
    public Team getTeam() {
        return this.team;
    }

    /**
     * Priradi uzivateli tym, ve kterem se nachazi
     * 
     * @param _team Novy tym
     */
    public void setTeam(Team _team) {
        this.team = _team;
    }

    /**
     * Navrati pristupovy token
     * 
     * @return Token
     */
    @JsonIgnore
    public String getToken() {
        return this.getToken();
    }

    /**
     * Nastavi novy token
     * 
     * @param _token Token
     */
    public void setToken(String _token) {
        this.token = _token;
    }

}

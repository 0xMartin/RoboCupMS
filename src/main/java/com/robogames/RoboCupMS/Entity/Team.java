package com.robogames.RoboCupMS.Entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.robogames.RoboCupMS.GlobalConfig;
import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Object.UserNameObj;
import com.robogames.RoboCupMS.Business.Object.YearObj;

/**
 * Tym pro soutezici
 */
@Entity(name = "team")
public class Team {

    /**
     * Navratova hodnota ID "zadneho" tymu (navrati pokud uzivatel neni v zadnem
     * tymu)
     */
    public static final int NOT_IN_TEAM = -1;

    /**
     * ID tymu
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Jmeno tymu (unikatni nazev)
     */
    @Column(name = "name", length = 40, nullable = false, unique = true)
    private String name;

    /**
     * Vedouci tymu
     */
    @OneToOne
    @JoinColumn(name = "leader_id", referencedColumnName = "id")
    private UserRC leader;

    /**
     * Clenove tymu (pokud je tym mazan neni mozne odstrani uzivatele)
     */
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<UserRC> members;

    /**
     * Registrace tymu do jednotlivych rocniku souteze
     */
    @OneToMany(mappedBy = "team", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<TeamRegistration> registrations;

    /**
     * Vytvori tym, do ktereho se muzou prihlasovat uzivatele. Do souteze se nemuze
     * prihlasit samotny uzivatel, vzdy musi soutezit v nejakem tymu.
     */
    public Team() {
        this.members = new ArrayList<UserRC>();
        this.registrations = new ArrayList<TeamRegistration>();
    }

    /**
     * Vytvori tym, do ktereho se muzou prihlasovat uzivatele. Do souteze se nemuze
     * prihlasit samotny uzivatel, vzdy musi soutezit v nejakem tymu.
     * 
     * @param _name   Jmeno tymu
     * @param _leader Vedouci tymu
     */
    public Team(String _name, UserRC _leader) {
        this.name = _name;
        this.leader = _leader;
        this.members = new ArrayList<UserRC>();
        this.registrations = new ArrayList<TeamRegistration>();
        this.addMember(_leader);
    }

    /**
     * Navrati ID tymu
     * 
     * @return ID tymu
     */
    public Long getID() {
        return this.id;
    }

    /**
     * Navrati jmeno tymu
     * 
     * @return Jmeno tymu
     */
    public String getName() {
        return this.name;
    }

    /**
     * Navrati ID, jmena a prijmeni vsech clenu, vcetne vedouciho
     * 
     * @return Clenove tymu
     */
    public List<UserNameObj> getMemberNames() {
        List<UserNameObj> userNames = new ArrayList<>();
        this.members.stream().forEach((u) -> {
            userNames.add(
                    new UserNameObj(
                            u.getID(),
                            u.getName(),
                            u.getSurname()));
        });
        return userNames;
    }

    /**
     * Navrati ID vedouciho tymu
     * 
     * @return Vedouci tymu
     */
    public long getLeaderID() {
        if (this.leader == null) {
            return -1;
        } else {
            return this.leader.getID();
        }
    }

    /**
     * Navrati ID vsech registraci
     * 
     * @return Registrace
     */
    public List<YearObj> getRegistrationYears() {
        List<YearObj> yearObjs = new ArrayList<>();
        this.registrations.stream().forEach((r) -> {
            yearObjs.add(
                    new YearObj(
                            r.getID(),
                            r.getCompatitionYear()));
        });
        return yearObjs;
    }

    /**
     * Navrati vsechny cleny tymu, vcetne vedouciho
     * 
     * @return Clenove tymu
     */
    @JsonIgnore
    public List<UserRC> getMembers() {
        return this.members;
    }

    /**
     * Navrati vsechny registrace tymu
     * 
     * @return Registrace
     */
    @JsonIgnore
    public List<TeamRegistration> getRegistrations() {
        return this.registrations;
    }

    /**
     * Nastavy nove jmeno tymu
     * 
     * @param _name Nove jmeno tymu
     */
    public void setName(String _name) {
        this.name = _name;
    }

    /**
     * Zmeni vedouciho tymu
     * 
     * @param _leader Novy vedouci tymu
     */
    public void setLeader(UserRC _leader) {
        this.leader = _leader;
    }

    /**
     * Do tymu prida noveho clena
     * 
     * @param _member Novy clen
     */
    public void addMember(UserRC _member) {
        if (_member != null) {
            this.members.add(_member);
            _member.setTeam(this);
        }
    }

    /**
     * Z tymu odebere clena
     * 
     * @param _member Novy clen
     */
    public void removeMember(UserRC _member) {
        if (_member != null) {
            this.members.remove(_member);
            _member.setTeam(null);
        }
    }

    /**
     * Urci kategorii tymu, ve ktere bude soutezit (dle vekou soutezicich)
     * 
     * @return Soutezni kategorie
     */
    public ECategory determinateCategory() {
        List<UserRC> users = this.getMembers();

        // vypocet veku nejstarsiho uzivatele v tymu
        int max_age = 0;
        for (UserRC u : users) {
            max_age = Math.max(max_age, u.getAge());
        }

        // podle veku urci kategorii
        if (max_age <= GlobalConfig.LOW_AGE_CATEGORY_MAX_AGE) {
            return ECategory.LOW_AGE_CATEGORY;
        } else {
            return ECategory.HIGH_AGE_CATEGORY;
        }
    }

}

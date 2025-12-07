package com.robogames.RoboCupMS.Module.CompetitionEvaluation.Bussiness.Object;

public class UserFullName {

    private String name;

    private String surname;

    /**
     * Cele jmeno uzivatele (souteziciho)
     * 
     * @param _name    Jmeno uzivatele
     * @param _surname Prijmeni uzivatele
     */
    public UserFullName(String _name, String _surname) {
        this.name = _name;
        this.surname = _surname;
    }

    /**
     * Navrati jmeno uzivatele
     * 
     * @return Jmeno
     */
    public String getName() {
        return this.name;
    }

    /**
     * Navrati prijmeni uzivatle
     * 
     * @return Prijmeni
     */
    public String getSurname() {
        return this.surname;
    }

}

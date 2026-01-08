package com.robogames.RoboCupMS.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Template pro diplomy
 */
@Entity(name = "diploma_template")
public class DiplomaTemplate {

    /**
     * ID template
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Obsah template (HTML/text)
     */
    @Column(columnDefinition = "TEXT")
    private String value;

    public DiplomaTemplate() {
    }

    public DiplomaTemplate(String value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
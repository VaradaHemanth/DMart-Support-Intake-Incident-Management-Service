package com.dmart.support_intake.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "signature_registry")
public class SignatureRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pattern_name", nullable = false, unique = true)
    private String patternName;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "default_severity")
    private String defaultSeverity;

    @Column(name = "standard_effort_mins")
    private Integer standardEffortMins;

    // Constructors
    public SignatureRegistry() {
    }

    public SignatureRegistry(Long id, String patternName, String category, String defaultSeverity, Integer standardEffortMins) {
        this.id = id;
        this.patternName = patternName;
        this.category = category;
        this.defaultSeverity = defaultSeverity;
        this.standardEffortMins = standardEffortMins;
    }

    // Explicit Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public String getCategory() { 
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDefaultSeverity() {
        return defaultSeverity;
    }

    public void setDefaultSeverity(String defaultSeverity) {
        this.defaultSeverity = defaultSeverity;
    }

    public Integer getStandardEffortMins() {
        return standardEffortMins;
    }

    public void setStandardEffortMins(Integer standardEffortMins) {
        this.standardEffortMins = standardEffortMins;
    }
}
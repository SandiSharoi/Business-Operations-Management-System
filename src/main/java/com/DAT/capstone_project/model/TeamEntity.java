package com.DAT.capstone_project.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.EqualsAndHashCode;


@Data
@Entity
@Table(name = "team")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "otherField"})  // Exclude fields that reference other entities

public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false) // This maps the department_id column to the Team entity
    private DepartmentEntity department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pm_id")
    @ToString.Exclude
    private UsersEntity pm;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dh_id")
    @ToString.Exclude
    private UsersEntity dh;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "divh_id")
    @ToString.Exclude
    private UsersEntity divh;

    public TeamEntity(String name, DepartmentEntity department) {
        this.name = name;
        this.department = department;
    }
}
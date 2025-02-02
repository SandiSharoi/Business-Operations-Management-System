package com.DAT.capstone_project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import jakarta.validation.constraints.Pattern;

@Data
@Entity
@Table(name = "users")
public class UsersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    @Column(name = "phone", nullable = false)
    @Pattern(regexp = "\\d+", message = "Phone number must contain only numbers")
    private String phone;

    @ToString.Exclude
    private String password; // Excluded from toString()

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_id")
    @ToString.Exclude
    private PositionEntity position;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    @ToString.Exclude
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    @ToString.Exclude
    private DepartmentEntity department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    @ToString.Exclude
    private RoleEntity role;
}

package com.toyproject.trollo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;

    @Column(nullable = false, unique = true, length = 100, columnDefinition = "VARCHAR(100)")
    private String email;

    @Column(nullable = false, length = 255, columnDefinition = "VARCHAR(255)")
    private String password;

    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private String nickname;

}

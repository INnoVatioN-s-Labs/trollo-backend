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
@Table(name = "tickets")
public class Ticket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;

    @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100)")
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "INT")
    private int position;

}

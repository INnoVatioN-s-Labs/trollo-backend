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
@Table(
    name = "boards",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_board_workspace_position", columnNames = {"workspace_id", "position"})
    },
    indexes = {
        @Index(name = "idx_board_workspace_position", columnList = "workspace_id, position")
    }
)
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;

    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private String name;

    @Column(nullable = false, columnDefinition = "INT")
    private int position;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false, columnDefinition = "BIGINT")
    private Workspace workspace;

    public void updatePosition(int position) {
        this.position = position;
    }

}

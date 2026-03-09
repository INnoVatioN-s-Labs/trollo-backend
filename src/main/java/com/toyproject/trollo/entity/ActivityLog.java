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
    name = "activity_logs",
    indexes = {
        @Index(name = "idx_activity_workspace_created_at", columnList = "workspace_id, created_at"),
        @Index(name = "idx_activity_user", columnList = "user_id")
    }
)
public class ActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false, columnDefinition = "BIGINT")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BIGINT")
    private User user;

    @Column(nullable = false, length = 255, columnDefinition = "VARCHAR(255)")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40, columnDefinition = "VARCHAR(40)")
    private ActivityType type;
}

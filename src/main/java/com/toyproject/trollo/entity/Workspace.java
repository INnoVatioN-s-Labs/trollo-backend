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
    name = "workspaces",
    indexes = {
        @Index(name = "idx_workspace_invite_code", columnList = "invite_code", unique = true)
    }
)
public class Workspace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;

    @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100)")
    private String name;

    @Column(length = 255, columnDefinition = "VARCHAR(255)")
    private String description;

    @Column(name = "invite_code", nullable = false, unique = true, length = 8, columnDefinition = "VARCHAR(8)")
    private String inviteCode;

    public void updateInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

}

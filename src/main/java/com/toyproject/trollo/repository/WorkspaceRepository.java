package com.toyproject.trollo.repository;

import com.toyproject.trollo.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Workspace> findByInviteCode(String inviteCode);
}

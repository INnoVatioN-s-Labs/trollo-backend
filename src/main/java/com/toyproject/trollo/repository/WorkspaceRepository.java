package com.toyproject.trollo.repository;

import com.toyproject.trollo.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    List<Workspace> findAllByOwnerIdOrderByIdDesc(Long ownerId);
}

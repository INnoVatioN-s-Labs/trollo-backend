package com.toyproject.trollo.repository;

import com.toyproject.trollo.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop10ByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);
    
    List<ActivityLog> findTop10ByWorkspaceIdInOrderByCreatedAtDesc(List<Long> workspaceIds);
}

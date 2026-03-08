package com.toyproject.trollo.repository;

import com.toyproject.trollo.entity.Membership;
import com.toyproject.trollo.entity.MembershipRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    Optional<Membership> findByWorkspaceIdAndUserIdAndRole(Long workspaceId, Long userId, MembershipRole role);

    List<Membership> findAllByUserIdOrderByWorkspaceIdDesc(Long userId);

    List<Membership> findAllByWorkspaceIdOrderByJoinedAtAsc(Long workspaceId);

    long countByWorkspaceId(Long workspaceId);

    long countByWorkspaceIdAndRole(Long workspaceId, MembershipRole role);
}

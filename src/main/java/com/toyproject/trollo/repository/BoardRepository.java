package com.toyproject.trollo.repository;

import com.toyproject.trollo.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("select coalesce(max(b.position), 0) from Board b where b.workspace.id = :workspaceId")
    int findMaxPositionByWorkspaceId(Long workspaceId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Board b
               set b.position = b.position + 1
             where b.workspace.id = :workspaceId
               and b.position >= :targetPosition
               and b.position < :currentPosition
            """)
    int shiftRight(Long workspaceId, int currentPosition, int targetPosition);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Board b
               set b.position = b.position - 1
             where b.workspace.id = :workspaceId
               and b.position <= :targetPosition
               and b.position > :currentPosition
            """)
    int shiftLeft(Long workspaceId, int currentPosition, int targetPosition);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Board b
               set b.position = b.position - 1
             where b.workspace.id = :workspaceId
               and b.position > :position
            """)
    int closeGap(Long workspaceId, int position);

    List<Board> findAllByWorkspaceIdOrderByPositionAsc(Long workspaceId);
}

package com.toyproject.trollo.repository;

import com.toyproject.trollo.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("select coalesce(max(t.position), 0) from Ticket t where t.board.id = :boardId")
    int findMaxPositionByBoardId(Long boardId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Ticket t
               set t.position = t.position + 1
             where t.board.id = :boardId
               and t.position >= :targetPosition
               and t.position < :currentPosition
            """)
    int shiftRight(Long boardId, int currentPosition, int targetPosition);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Ticket t
               set t.position = t.position - 1
             where t.board.id = :boardId
               and t.position <= :targetPosition
               and t.position > :currentPosition
            """)
    int shiftLeft(Long boardId, int currentPosition, int targetPosition);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Ticket t
               set t.position = t.position - 1
             where t.board.id = :boardId
               and t.position > :position
            """)
    int closeGap(Long boardId, int position);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Ticket t
               set t.position = t.position + 1
             where t.board.id = :boardId
               and t.position >= :targetPosition
            """)
    int makeRoom(Long boardId, int targetPosition);

    @Query("""
            select t
              from Ticket t
              join fetch t.board b
             where b.workspace.id = :workspaceId
             order by b.position asc, t.position asc
            """)
    List<Ticket> findAllForKanbanByWorkspaceId(Long workspaceId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Ticket t where t.board.id = :boardId")
    int deleteByBoardId(Long boardId);
}

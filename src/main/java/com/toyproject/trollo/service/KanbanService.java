package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.kanban.KanbanBoardResponse;
import com.toyproject.trollo.dto.kanban.KanbanTicketResponse;
import com.toyproject.trollo.dto.kanban.KanbanWorkspaceResponse;
import com.toyproject.trollo.entity.Board;
import com.toyproject.trollo.entity.Ticket;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
import com.toyproject.trollo.repository.BoardRepository;
import com.toyproject.trollo.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KanbanService {

    private final BoardRepository boardRepository;
    private final TicketRepository ticketRepository;
    private final WorkspaceAccessService workspaceAccessService;

    @Transactional(readOnly = true)
    public KanbanWorkspaceResponse getKanban(String ownerEmail, Long workspaceId) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        Workspace workspace = workspaceAccessService.getWorkspace(workspaceId);
        workspaceAccessService.getMembership(workspaceId, owner.getId());

        List<Board> boards = boardRepository.findAllByWorkspaceIdOrderByPositionAsc(workspaceId);
        Map<Long, KanbanBoardAccumulator> boardMap = new LinkedHashMap<>();

        for (Board board : boards) {
            boardMap.put(board.getId(), new KanbanBoardAccumulator(board));
        }

        List<Ticket> tickets = ticketRepository.findAllForKanbanByWorkspaceId(workspaceId);
        for (Ticket ticket : tickets) {
            KanbanBoardAccumulator accumulator = boardMap.get(ticket.getBoard().getId());
            if (accumulator != null) {
                accumulator.tickets().add(new KanbanTicketResponse(
                        ticket.getId(),
                        ticket.getTitle(),
                        ticket.getDescription(),
                        ticket.getPosition()
                ));
            }
        }

        List<KanbanBoardResponse> boardResponses = boardMap.values().stream()
                .map(acc -> new KanbanBoardResponse(
                        acc.board().getId(),
                        acc.board().getName(),
                        acc.board().getPosition(),
                        acc.tickets()
                ))
                .toList();

        return new KanbanWorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                boardResponses
        );
    }

    private record KanbanBoardAccumulator(Board board, List<KanbanTicketResponse> tickets) {
        private KanbanBoardAccumulator(Board board) {
            this(board, new ArrayList<>());
        }
    }
}

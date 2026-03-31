package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.ticket.CreateTicketRequest;
import com.toyproject.trollo.dto.ticket.MoveTicketRequest;
import com.toyproject.trollo.dto.ticket.TicketResponse;
import com.toyproject.trollo.dto.ticket.UpdateTicketRequest;
import com.toyproject.trollo.entity.ActivityType;
import com.toyproject.trollo.entity.Board;
import com.toyproject.trollo.entity.Ticket;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
import com.toyproject.trollo.repository.BoardRepository;
import com.toyproject.trollo.repository.TicketRepository;
import com.toyproject.trollo.repository.CommentRepository;
import com.toyproject.trollo.repository.ActivityLogRepository;
import com.toyproject.trollo.entity.Comment;
import com.toyproject.trollo.entity.ActivityLog;
import com.toyproject.trollo.dto.ticket.TicketFeedResponse;
import com.toyproject.trollo.dto.ticket.FeedType;
import java.util.List;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final BoardRepository boardRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final ActivityLogService activityLogService;
    private final CommentRepository commentRepository;
    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public TicketResponse createTicket(String ownerEmail, Long workspaceId, Long boardId, CreateTicketRequest request) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        Workspace workspace = workspaceAccessService.getWorkspace(workspaceId);
        workspaceAccessService.getMembership(workspaceId, owner.getId());
        Board board = getBoardWithWorkspaceCheck(boardId, workspaceId);

        int nextPosition = ticketRepository.findMaxPositionByBoardId(boardId) + 1;
        Ticket savedTicket = ticketRepository.save(Ticket.builder()
                .title(request.title())
                .description(request.description())
                .position(nextPosition)
                .board(board)
                .build());
        activityLogService.saveLog(workspace, owner, savedTicket, ActivityType.TICKET_CREATE, "티켓을 생성했습니다: " + savedTicket.getTitle());
        return toResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(String ownerEmail, Long workspaceId, Long boardId, Long ticketId) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        workspaceAccessService.getMembership(workspaceId, owner.getId());
        getBoardWithWorkspaceCheck(boardId, workspaceId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
        validateTicketBoard(ticket, boardId);

        return toResponse(ticket);
    }

    @Transactional
    public TicketResponse updateTicket(
            String ownerEmail,
            Long workspaceId,
            Long boardId,
            Long ticketId,
            UpdateTicketRequest request
    ) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        Workspace workspace = workspaceAccessService.getWorkspace(workspaceId);
        workspaceAccessService.getMembership(workspaceId, owner.getId());
        getBoardWithWorkspaceCheck(boardId, workspaceId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
        validateTicketBoard(ticket, boardId);

        ticket.updateContent(request.title(), request.description());
        Ticket updatedTicket = ticketRepository.save(ticket);
        activityLogService.saveLog(workspace, owner, updatedTicket, ActivityType.TICKET_UPDATE, "티켓을 수정했습니다: " + updatedTicket.getTitle());
        return toResponse(updatedTicket);
    }

    @Transactional
    public TicketResponse moveTicket(
            String ownerEmail,
            Long workspaceId,
            Long boardId,
            Long ticketId,
            MoveTicketRequest request
    ) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        Workspace workspace = workspaceAccessService.getWorkspace(workspaceId);
        workspaceAccessService.getMembership(workspaceId, owner.getId());

        Board sourceBoard = getBoardWithWorkspaceCheck(boardId, workspaceId);
        Board targetBoard = getBoardWithWorkspaceCheck(request.targetBoardId(), workspaceId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
        validateTicketBoard(ticket, sourceBoard.getId());

        int currentPosition = ticket.getPosition();
        int targetPosition = request.targetPosition();
        boolean sameBoardMove = sourceBoard.getId().equals(targetBoard.getId());

        if (sameBoardMove) {
            int maxPosition = ticketRepository.findMaxPositionByBoardId(sourceBoard.getId());
            validateSameBoardTargetPosition(targetPosition, maxPosition);

            if (currentPosition == targetPosition) {
                return toResponse(ticket);
            }

            ticket.updatePosition(0);
            ticketRepository.flush();

            if (targetPosition < currentPosition) {
                ticketRepository.shiftRight(sourceBoard.getId(), currentPosition, targetPosition);
            } else {
                ticketRepository.shiftLeft(sourceBoard.getId(), currentPosition, targetPosition);
            }

            ticket.updatePosition(targetPosition);
        } else {
            int targetMaxPosition = ticketRepository.findMaxPositionByBoardId(targetBoard.getId());
            validateCrossBoardTargetPosition(targetPosition, targetMaxPosition);

            ticket.updatePosition(0);
            ticketRepository.flush();

            ticketRepository.closeGap(sourceBoard.getId(), currentPosition);
            ticketRepository.makeRoom(targetBoard.getId(), targetPosition);
            ticket.moveTo(targetBoard, targetPosition);
        }

        Ticket movedTicket = ticketRepository.save(ticket);
        activityLogService.saveLog(workspace, owner, movedTicket, ActivityType.TICKET_MOVE, "티켓을 이동했습니다: " + movedTicket.getTitle());
        return toResponse(movedTicket);
    }

    @Transactional
    public void deleteTicket(String ownerEmail, Long workspaceId, Long boardId, Long ticketId) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        Workspace workspace = workspaceAccessService.getWorkspace(workspaceId);
        workspaceAccessService.getMembership(workspaceId, owner.getId());
        getBoardWithWorkspaceCheck(boardId, workspaceId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
        validateTicketBoard(ticket, boardId);

        int currentPosition = ticket.getPosition();
        ticketRepository.delete(ticket);
        ticketRepository.flush();
        ticketRepository.closeGap(boardId, currentPosition);
        activityLogService.saveLog(workspace, owner, ActivityType.TICKET_DELETE, "티켓을 삭제했습니다: " + ticket.getTitle());
    }

    @Transactional(readOnly = true)
    public List<TicketFeedResponse> getTicketFeeds(
            String ownerEmail, Long workspaceId, Long boardId, Long ticketId) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        workspaceAccessService.getMembership(workspaceId, owner.getId());
        getBoardWithWorkspaceCheck(boardId, workspaceId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
        validateTicketBoard(ticket, boardId);

        List<Comment> comments = commentRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
        List<ActivityLog> logs = activityLogRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);

        List<TicketFeedResponse> feeds = new ArrayList<>();
        
        for (Comment c : comments) {
            feeds.add(new TicketFeedResponse(
                    c.getId(),
                    FeedType.COMMENT,
                    null,
                    c.getContent(),
                    c.getUser().getId(),
                    c.getUser().getNickname(),
                    c.getCreatedAt()
            ));
        }

        for (ActivityLog log : logs) {
            feeds.add(new TicketFeedResponse(
                    log.getId(),
                    FeedType.ACTIVITY,
                    log.getType(),
                    log.getContent(),
                    log.getUser().getId(),
                    log.getUser().getNickname(),
                    log.getCreatedAt()
            ));
        }

        feeds.sort((f1, f2) -> f2.createdAt().compareTo(f1.createdAt()));
        return feeds;
    }

    private Board getBoardWithWorkspaceCheck(Long boardId, Long workspaceId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
        if (!board.getWorkspace().getId().equals(workspaceId)) {
            throw new BusinessException(ErrorCode.BOARD_WORKSPACE_MISMATCH);
        }
        return board;
    }

    private void validateTicketBoard(Ticket ticket, Long boardId) {
        if (!ticket.getBoard().getId().equals(boardId)) {
            throw new BusinessException(ErrorCode.TICKET_BOARD_MISMATCH);
        }
    }

    private void validateSameBoardTargetPosition(int targetPosition, int maxPosition) {
        if (targetPosition < 1 || targetPosition > maxPosition) {
            throw new BusinessException(ErrorCode.TICKET_INVALID_POSITION);
        }
    }

    private void validateCrossBoardTargetPosition(int targetPosition, int targetMaxPosition) {
        if (targetPosition < 1 || targetPosition > targetMaxPosition + 1) {
            throw new BusinessException(ErrorCode.TICKET_INVALID_POSITION);
        }
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPosition(),
                ticket.getBoard().getId()
        );
    }

}

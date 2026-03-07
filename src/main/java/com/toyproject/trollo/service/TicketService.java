package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.ticket.CreateTicketRequest;
import com.toyproject.trollo.dto.ticket.MoveTicketRequest;
import com.toyproject.trollo.dto.ticket.TicketResponse;
import com.toyproject.trollo.dto.ticket.UpdateTicketRequest;
import com.toyproject.trollo.entity.Board;
import com.toyproject.trollo.entity.Ticket;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
import com.toyproject.trollo.repository.BoardRepository;
import com.toyproject.trollo.repository.TicketRepository;
import com.toyproject.trollo.repository.UserRepository;
import com.toyproject.trollo.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final BoardRepository boardRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Transactional
    public TicketResponse createTicket(String ownerEmail, Long workspaceId, Long boardId, CreateTicketRequest request) {
        User owner = getUserByEmail(ownerEmail);
        getWorkspaceWithAccessCheck(workspaceId, owner.getId());
        Board board = getBoardWithWorkspaceCheck(boardId, workspaceId);

        int nextPosition = ticketRepository.findMaxPositionByBoardId(boardId) + 1;
        Ticket savedTicket = ticketRepository.save(Ticket.builder()
                .title(request.title())
                .description(request.description())
                .position(nextPosition)
                .board(board)
                .build());

        return toResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(String ownerEmail, Long workspaceId, Long boardId, Long ticketId) {
        User owner = getUserByEmail(ownerEmail);
        getWorkspaceWithAccessCheck(workspaceId, owner.getId());
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
        User owner = getUserByEmail(ownerEmail);
        getWorkspaceWithAccessCheck(workspaceId, owner.getId());
        getBoardWithWorkspaceCheck(boardId, workspaceId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
        validateTicketBoard(ticket, boardId);

        ticket.updateContent(request.title(), request.description());
        Ticket updatedTicket = ticketRepository.save(ticket);
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
        User owner = getUserByEmail(ownerEmail);
        getWorkspaceWithAccessCheck(workspaceId, owner.getId());

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
        return toResponse(movedTicket);
    }

    @Transactional
    public void deleteTicket(String ownerEmail, Long workspaceId, Long boardId, Long ticketId) {
        User owner = getUserByEmail(ownerEmail);
        getWorkspaceWithAccessCheck(workspaceId, owner.getId());
        getBoardWithWorkspaceCheck(boardId, workspaceId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
        validateTicketBoard(ticket, boardId);

        int currentPosition = ticket.getPosition();
        ticketRepository.delete(ticket);
        ticketRepository.flush();
        ticketRepository.closeGap(boardId, currentPosition);
    }

    private User getUserByEmail(String ownerEmail) {
        return userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Workspace getWorkspaceWithAccessCheck(Long workspaceId, Long ownerId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_NOT_FOUND));
        if (!workspace.getOwner().getId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
        return workspace;
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

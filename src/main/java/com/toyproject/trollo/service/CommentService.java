package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.ticket.CreateCommentRequest;
import com.toyproject.trollo.entity.Comment;
import com.toyproject.trollo.entity.Ticket;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.repository.CommentRepository;
import com.toyproject.trollo.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final WorkspaceAccessService workspaceAccessService;

    @Transactional
    public void createComment(String ownerEmail, Long workspaceId, Long boardId, Long ticketId, CreateCommentRequest request) {
        User user = workspaceAccessService.getUserByEmail(ownerEmail);
        workspaceAccessService.getMembership(workspaceId, user.getId());

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

        // Note: We use basic validation since fine-grained validation exists in TicketService and others
        if (!ticket.getBoard().getId().equals(boardId)) {
            throw new BusinessException(ErrorCode.TICKET_BOARD_MISMATCH);
        }

        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
        }

        Comment comment = Comment.builder()
                .ticket(ticket)
                .user(user)
                .content(request.content())
                .parent(parent)
                .build();

        commentRepository.save(comment);
    }
}

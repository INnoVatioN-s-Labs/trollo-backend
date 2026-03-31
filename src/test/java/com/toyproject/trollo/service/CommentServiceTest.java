package com.toyproject.trollo.service;

import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.ticket.CreateCommentRequest;
import com.toyproject.trollo.entity.Board;
import com.toyproject.trollo.entity.Comment;
import com.toyproject.trollo.entity.Ticket;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
import com.toyproject.trollo.repository.CommentRepository;
import com.toyproject.trollo.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private WorkspaceAccessService workspaceAccessService;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("댓글 추가 성공")
    void createCommentSuccess() {
        String email = "user@example.com";
        User user = User.builder().id(1L).email(email).build();
        Workspace workspace = Workspace.builder().id(10L).build();
        Board board = Board.builder().id(100L).workspace(workspace).build();
        Ticket ticket = Ticket.builder().id(1000L).board(board).build();

        given(workspaceAccessService.getUserByEmail(email)).willReturn(user);
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));

        commentService.createComment(email, 10L, 100L, 1000L, new CreateCommentRequest("댓글입니다", null));

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 추가 성공")
    void createReplyCommentSuccess() {
        String email = "user@example.com";
        User user = User.builder().id(1L).email(email).build();
        Workspace workspace = Workspace.builder().id(10L).build();
        Board board = Board.builder().id(100L).workspace(workspace).build();
        Ticket ticket = Ticket.builder().id(1000L).board(board).build();
        Comment parentComment = Comment.builder().id(50L).build();

        given(workspaceAccessService.getUserByEmail(email)).willReturn(user);
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));
        given(commentRepository.findById(50L)).willReturn(Optional.of(parentComment));

        commentService.createComment(email, 10L, 100L, 1000L, new CreateCommentRequest("대댓글입니다", 50L));

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("티켓이 속한 보드가 다를 경우 예외 발생")
    void createCommentFailsWhenBoardMismatch() {
        String email = "user@example.com";
        User user = User.builder().id(1L).email(email).build();
        Workspace workspace = Workspace.builder().id(10L).build();
        Board board = Board.builder().id(999L).workspace(workspace).build(); // Board UUID mismatch
        Ticket ticket = Ticket.builder().id(1000L).board(board).build();

        given(workspaceAccessService.getUserByEmail(email)).willReturn(user);
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));

        assertThatThrownBy(() -> 
            commentService.createComment(email, 10L, 100L, 1000L, new CreateCommentRequest("댓글", null))
        ).isInstanceOf(BusinessException.class);
    }
}

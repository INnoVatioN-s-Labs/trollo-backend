package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.ticket.CreateTicketRequest;
import com.toyproject.trollo.dto.ticket.MoveTicketRequest;
import com.toyproject.trollo.dto.ticket.TicketResponse;
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
import com.toyproject.trollo.entity.ActivityType;
import com.toyproject.trollo.dto.ticket.TicketFeedResponse;
import com.toyproject.trollo.dto.ticket.FeedType;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private WorkspaceAccessService workspaceAccessService;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    @DisplayName("티켓 생성 성공 시 보드 마지막 위치 + 1로 저장한다")
    void createTicketSuccess() {
        String email = "user@example.com";
        User user = createUser(1L, email);
        Workspace workspace = createWorkspace(10L);
        Board board = createBoard(100L, workspace, 1);

        given(workspaceAccessService.getUserByEmail(email)).willReturn(user);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));
        given(ticketRepository.findMaxPositionByBoardId(100L)).willReturn(2);
        given(ticketRepository.save(any(Ticket.class))).willAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            return Ticket.builder()
                    .id(1000L)
                    .title(ticket.getTitle())
                    .description(ticket.getDescription())
                    .position(ticket.getPosition())
                    .board(ticket.getBoard())
                    .build();
        });

        TicketResponse response = ticketService.createTicket(email, 10L, 100L, new CreateTicketRequest("제목", "설명"));

        assertThat(response.id()).isEqualTo(1000L);
        assertThat(response.position()).isEqualTo(3);
        verify(activityLogService).saveLog(any(Workspace.class), any(User.class), any(Ticket.class), any(), any(String.class));
    }

    @Test
    @DisplayName("티켓 이동 시 유효하지 않은 위치면 예외가 발생한다")
    void moveTicketFailsWhenTargetPositionInvalid() {
        String email = "user@example.com";
        User user = createUser(1L, email);
        Workspace workspace = createWorkspace(10L);
        Board sourceBoard = createBoard(100L, workspace, 1);
        Board targetBoard = createBoard(101L, workspace, 2);
        Ticket ticket = Ticket.builder().id(1000L).title("제목").description("설명").position(2).board(sourceBoard).build();

        given(workspaceAccessService.getUserByEmail(email)).willReturn(user);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);
        given(boardRepository.findById(100L)).willReturn(Optional.of(sourceBoard));
        given(boardRepository.findById(101L)).willReturn(Optional.of(targetBoard));
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));
        given(ticketRepository.findMaxPositionByBoardId(101L)).willReturn(3);

        assertThatThrownBy(() -> ticketService.moveTicket(
                email,
                10L,
                100L,
                1000L,
                new MoveTicketRequest(101L, 5)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TICKET_INVALID_POSITION);
    }

    private User createUser(Long id, String email) {
        return User.builder().id(id).email(email).password("pw").nickname("사용자").build();
    }

    private Workspace createWorkspace(Long id) {
        return Workspace.builder().id(id).name("백엔드").description("설명").inviteCode("AB12CD34").build();
    }

    @Test
    @DisplayName("티켓 통합 피드(댓글+활동기록) 조회 성공 시 시간순으로 정렬된다")
    void getTicketFeedsSuccess() {
        String email = "user@example.com";
        User user = createUser(1L, email);
        User user2 = createUser(2L, "other@example.com");
        Workspace workspace = createWorkspace(10L);
        Board board = createBoard(100L, workspace, 1);
        Ticket ticket = Ticket.builder().id(1000L).title("제목").description("설명").position(1).board(board).build();

        Comment comment = Comment.builder()
                .id(1L).ticket(ticket).user(user2).content("댓글")
                .build();
        // BaseEntity fields aren't easily mutable without reflection in pure tests, so we may not be able to rely on createdAt easily, 
        // but since we are mocking the repository return values, the service will just sort them.

        ActivityLog log = ActivityLog.builder()
                .id(100L).workspace(workspace).user(user).ticket(ticket).type(ActivityType.TICKET_UPDATE).content("수정")
                .build();

        given(workspaceAccessService.getUserByEmail(email)).willReturn(user);
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));
        given(commentRepository.findByTicketIdOrderByCreatedAtDesc(1000L)).willReturn(List.of(comment));
        given(activityLogRepository.findByTicketIdOrderByCreatedAtDesc(1000L)).willReturn(List.of(log));

        // reflection is better:
        ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.now().minusMinutes(10));
        ReflectionTestUtils.setField(log, "createdAt", LocalDateTime.now());

        List<TicketFeedResponse> feeds = ticketService.getTicketFeeds(email, 10L, 100L, 1000L);
        assertThat(feeds).hasSize(2);
        assertThat(feeds.get(0).type()).isEqualTo(FeedType.ACTIVITY);
        assertThat(feeds.get(1).type()).isEqualTo(FeedType.COMMENT);
    }

    private Board createBoard(Long id, Workspace workspace, int position) {
        return Board.builder().id(id).name("Todo").position(position).workspace(workspace).build();
    }
}

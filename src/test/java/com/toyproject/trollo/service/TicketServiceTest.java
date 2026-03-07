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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    @DisplayName("티켓 생성 성공 시 보드 마지막 위치 + 1로 저장한다")
    void createTicketSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board board = createBoard(100L, 1, workspace);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
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

        TicketResponse response = ticketService.createTicket(
                ownerEmail,
                10L,
                100L,
                new CreateTicketRequest("티켓 제목", "티켓 설명")
        );

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(ticketCaptor.capture());

        assertThat(ticketCaptor.getValue().getPosition()).isEqualTo(3);
        assertThat(response.id()).isEqualTo(1000L);
        assertThat(response.position()).isEqualTo(3);
        assertThat(response.boardId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("티켓 생성 시 다른 소유자의 워크스페이스면 예외가 발생한다")
    void createTicketFailsWhenWorkspaceAccessDenied() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        User other = createUser(2L, "other@example.com");
        Workspace workspace = createWorkspace(10L, other);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));

        assertThatThrownBy(() -> ticketService.createTicket(
                ownerEmail,
                10L,
                100L,
                new CreateTicketRequest("티켓 제목", "티켓 설명")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_ACCESS_DENIED);

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("티켓 조회 성공 시 티켓 내용을 반환한다")
    void getTicketSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board board = createBoard(100L, 1, workspace);
        Ticket ticket = createTicket(1000L, "제목", "설명", 1, board);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));

        TicketResponse response = ticketService.getTicket(ownerEmail, 10L, 100L, 1000L);

        assertThat(response.id()).isEqualTo(1000L);
        assertThat(response.title()).isEqualTo("제목");
        assertThat(response.description()).isEqualTo("설명");
        assertThat(response.position()).isEqualTo(1);
    }

    @Test
    @DisplayName("티켓 조회 시 보드가 다르면 예외가 발생한다")
    void getTicketFailsWhenBoardMismatch() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board board = createBoard(100L, 1, workspace);
        Board otherBoard = createBoard(101L, 2, workspace);
        Ticket ticket = createTicket(1000L, "제목", "설명", 1, otherBoard);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.getTicket(ownerEmail, 10L, 100L, 1000L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TICKET_BOARD_MISMATCH);
    }

    @Test
    @DisplayName("티켓 수정 성공 시 제목/설명이 변경된다")
    void updateTicketSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board board = createBoard(100L, 1, workspace);
        Ticket ticket = createTicket(1000L, "기존 제목", "기존 설명", 1, board);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));
        given(ticketRepository.save(any(Ticket.class))).willAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.updateTicket(
                ownerEmail,
                10L,
                100L,
                1000L,
                new UpdateTicketRequest("변경 제목", "변경 설명")
        );

        assertThat(response.title()).isEqualTo("변경 제목");
        assertThat(response.description()).isEqualTo("변경 설명");
    }

    @Test
    @DisplayName("티켓 수정 시 티켓이 없으면 예외가 발생한다")
    void updateTicketFailsWhenTicketNotFound() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board board = createBoard(100L, 1, workspace);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));
        given(ticketRepository.findById(1000L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.updateTicket(
                ownerEmail,
                10L,
                100L,
                1000L,
                new UpdateTicketRequest("변경 제목", "변경 설명")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TICKET_NOT_FOUND);

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("같은 보드 내 티켓 이동 성공 시 위치를 재배치한다")
    void moveTicketWithinBoardSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board board = createBoard(100L, 1, workspace);
        Ticket ticket = createTicket(1000L, "제목", "설명", 3, board);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));
        given(ticketRepository.findMaxPositionByBoardId(100L)).willReturn(4);
        given(ticketRepository.save(any(Ticket.class))).willAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.moveTicket(
                ownerEmail,
                10L,
                100L,
                1000L,
                new MoveTicketRequest(100L, 1)
        );

        verify(ticketRepository).flush();
        verify(ticketRepository).shiftRight(100L, 3, 1);
        assertThat(response.position()).isEqualTo(1);
        assertThat(response.boardId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("다른 보드로 티켓 이동 성공 시 소스 보드 갭 제거와 타겟 보드 밀어내기를 수행한다")
    void moveTicketAcrossBoardSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board sourceBoard = createBoard(100L, 1, workspace);
        Board targetBoard = createBoard(101L, 2, workspace);
        Ticket ticket = createTicket(1000L, "제목", "설명", 2, sourceBoard);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findById(100L)).willReturn(Optional.of(sourceBoard));
        given(boardRepository.findById(101L)).willReturn(Optional.of(targetBoard));
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));
        given(ticketRepository.findMaxPositionByBoardId(101L)).willReturn(3);
        given(ticketRepository.save(any(Ticket.class))).willAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.moveTicket(
                ownerEmail,
                10L,
                100L,
                1000L,
                new MoveTicketRequest(101L, 2)
        );

        verify(ticketRepository).flush();
        verify(ticketRepository).closeGap(100L, 2);
        verify(ticketRepository).makeRoom(101L, 2);
        assertThat(response.position()).isEqualTo(2);
        assertThat(response.boardId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("티켓 이동 시 목표 위치가 범위를 벗어나면 예외가 발생한다")
    void moveTicketFailsWhenTargetPositionInvalid() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board sourceBoard = createBoard(100L, 1, workspace);
        Board targetBoard = createBoard(101L, 2, workspace);
        Ticket ticket = createTicket(1000L, "제목", "설명", 2, sourceBoard);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findById(100L)).willReturn(Optional.of(sourceBoard));
        given(boardRepository.findById(101L)).willReturn(Optional.of(targetBoard));
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));
        given(ticketRepository.findMaxPositionByBoardId(101L)).willReturn(3);

        assertThatThrownBy(() -> ticketService.moveTicket(
                ownerEmail,
                10L,
                100L,
                1000L,
                new MoveTicketRequest(101L, 5)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TICKET_INVALID_POSITION);

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("티켓 삭제 성공 시 티켓을 삭제하고 보드 내 위치를 정리한다")
    void deleteTicketSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board board = createBoard(100L, 1, workspace);
        Ticket ticket = createTicket(1000L, "제목", "설명", 2, board);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));

        ticketService.deleteTicket(ownerEmail, 10L, 100L, 1000L);

        verify(ticketRepository).delete(ticket);
        verify(ticketRepository).flush();
        verify(ticketRepository).closeGap(100L, 2);
    }

    @Test
    @DisplayName("티켓 삭제 시 보드가 다르면 예외가 발생한다")
    void deleteTicketFailsWhenBoardMismatch() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board board = createBoard(100L, 1, workspace);
        Board otherBoard = createBoard(101L, 2, workspace);
        Ticket ticket = createTicket(1000L, "제목", "설명", 2, otherBoard);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));
        given(ticketRepository.findById(1000L)).willReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.deleteTicket(ownerEmail, 10L, 100L, 1000L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TICKET_BOARD_MISMATCH);

        verify(ticketRepository, never()).delete(any(Ticket.class));
        verify(ticketRepository, never()).closeGap(100L, 2);
    }

    private User createUser(Long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .password("encoded-password")
                .nickname("사용자")
                .build();
    }

    private Workspace createWorkspace(Long id, User owner) {
        return Workspace.builder()
                .id(id)
                .name("백엔드")
                .description("백엔드 작업 공간")
                .owner(owner)
                .build();
    }

    private Board createBoard(Long id, int position, Workspace workspace) {
        return Board.builder()
                .id(id)
                .name("Todo")
                .position(position)
                .workspace(workspace)
                .build();
    }

    private Ticket createTicket(Long id, String title, String description, int position, Board board) {
        return Ticket.builder()
                .id(id)
                .title(title)
                .description(description)
                .position(position)
                .board(board)
                .build();
    }
}

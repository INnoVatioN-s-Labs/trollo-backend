package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.kanban.KanbanWorkspaceResponse;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KanbanServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private KanbanService kanbanService;

    @Test
    @DisplayName("칸반 통합 조회 성공 시 보드와 티켓을 위치 순서로 반환한다")
    void getKanbanSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board todo = createBoard(101L, "Todo", 1, workspace);
        Board doing = createBoard(102L, "Doing", 2, workspace);

        Ticket todoTicket = createTicket(1001L, "요구사항 정리", 1, todo);
        Ticket doingTicket = createTicket(1002L, "API 구현", 1, doing);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findAllByWorkspaceIdOrderByPositionAsc(10L)).willReturn(List.of(todo, doing));
        given(ticketRepository.findAllForKanbanByWorkspaceId(10L)).willReturn(List.of(todoTicket, doingTicket));

        KanbanWorkspaceResponse response = kanbanService.getKanban(ownerEmail, 10L);

        assertThat(response.workspaceId()).isEqualTo(10L);
        assertThat(response.boards()).hasSize(2);
        assertThat(response.boards().get(0).name()).isEqualTo("Todo");
        assertThat(response.boards().get(0).tickets()).hasSize(1);
        assertThat(response.boards().get(0).tickets().get(0).title()).isEqualTo("요구사항 정리");
        assertThat(response.boards().get(1).name()).isEqualTo("Doing");
        assertThat(response.boards().get(1).tickets()).hasSize(1);
        assertThat(response.boards().get(1).tickets().get(0).title()).isEqualTo("API 구현");
    }

    @Test
    @DisplayName("칸반 통합 조회 시 워크스페이스 소유자가 다르면 예외가 발생한다")
    void getKanbanFailsWhenWorkspaceAccessDenied() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        User other = createUser(2L, "other@example.com");
        Workspace workspace = createWorkspace(10L, other);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));

        assertThatThrownBy(() -> kanbanService.getKanban(ownerEmail, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_ACCESS_DENIED);
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

    private Board createBoard(Long id, String name, int position, Workspace workspace) {
        return Board.builder()
                .id(id)
                .name(name)
                .position(position)
                .workspace(workspace)
                .build();
    }

    private Ticket createTicket(Long id, String title, int position, Board board) {
        return Ticket.builder()
                .id(id)
                .title(title)
                .description(title + " 설명")
                .position(position)
                .board(board)
                .build();
    }
}

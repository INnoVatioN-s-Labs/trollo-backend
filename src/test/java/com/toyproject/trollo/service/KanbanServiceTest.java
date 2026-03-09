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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KanbanServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private WorkspaceAccessService workspaceAccessService;

    @InjectMocks
    private KanbanService kanbanService;

    @Test
    @DisplayName("칸반 조회 성공 시 보드/티켓을 반환한다")
    void getKanbanSuccess() {
        String email = "user@example.com";
        User user = createUser(1L, email);
        Workspace workspace = createWorkspace(10L);
        Board board = Board.builder().id(100L).name("Todo").position(1).workspace(workspace).build();
        Ticket ticket = Ticket.builder().id(1000L).title("작업").description("설명").position(1).board(board).build();

        given(workspaceAccessService.getUserByEmail(email)).willReturn(user);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);
        given(boardRepository.findAllByWorkspaceIdOrderByPositionAsc(10L)).willReturn(List.of(board));
        given(ticketRepository.findAllForKanbanByWorkspaceId(10L)).willReturn(List.of(ticket));

        KanbanWorkspaceResponse response = kanbanService.getKanban(email, 10L);

        assertThat(response.workspaceId()).isEqualTo(10L);
        assertThat(response.boards()).hasSize(1);
        assertThat(response.boards().get(0).tickets()).hasSize(1);
    }

    @Test
    @DisplayName("멤버가 아니면 칸반 조회 시 접근 거부 예외가 발생한다")
    void getKanbanFailsWhenAccessDenied() {
        String email = "user@example.com";
        User user = createUser(1L, email);

        given(workspaceAccessService.getUserByEmail(email)).willReturn(user);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(createWorkspace(10L));
        given(workspaceAccessService.getMembership(10L, 1L))
                .willThrow(new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        assertThatThrownBy(() -> kanbanService.getKanban(email, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_ACCESS_DENIED);
    }

    private User createUser(Long id, String email) {
        return User.builder().id(id).email(email).password("pw").nickname("사용자").build();
    }

    private Workspace createWorkspace(Long id) {
        return Workspace.builder().id(id).name("백엔드").description("설명").inviteCode("AB12CD34").build();
    }
}

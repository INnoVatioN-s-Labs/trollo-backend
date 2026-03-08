package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.board.BoardResponse;
import com.toyproject.trollo.dto.board.CreateBoardRequest;
import com.toyproject.trollo.dto.board.ReorderBoardRequest;
import com.toyproject.trollo.entity.Board;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private WorkspaceAccessService workspaceAccessService;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private BoardService boardService;

    @Test
    @DisplayName("보드 생성 성공 시 마지막 위치 + 1로 저장한다")
    void createBoardSuccess() {
        String email = "user@example.com";
        User user = createUser(1L, email);
        Workspace workspace = createWorkspace(10L);

        given(workspaceAccessService.getUserByEmail(email)).willReturn(user);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);
        given(boardRepository.findMaxPositionByWorkspaceId(10L)).willReturn(2);
        given(boardRepository.save(any(Board.class))).willAnswer(invocation -> {
            Board board = invocation.getArgument(0);
            return Board.builder()
                    .id(100L)
                    .name(board.getName())
                    .position(board.getPosition())
                    .workspace(board.getWorkspace())
                    .build();
        });

        BoardResponse response = boardService.createBoard(email, 10L, new CreateBoardRequest("Doing"));

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.position()).isEqualTo(3);
        verify(activityLogService).saveLog(any(Workspace.class), any(User.class), any(), any(String.class));
    }

    @Test
    @DisplayName("멤버가 아니면 보드 생성 시 접근 거부 예외가 발생한다")
    void createBoardFailsWhenAccessDenied() {
        String email = "user@example.com";
        User user = createUser(1L, email);
        Workspace workspace = createWorkspace(10L);

        given(workspaceAccessService.getUserByEmail(email)).willReturn(user);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);
        given(workspaceAccessService.getMembership(10L, 1L))
                .willThrow(new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        assertThatThrownBy(() -> boardService.createBoard(email, 10L, new CreateBoardRequest("Doing")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("보드 순서 변경 시 워크스페이스가 다르면 예외가 발생한다")
    void reorderBoardFailsWhenWorkspaceMismatch() {
        String email = "user@example.com";
        User user = createUser(1L, email);
        Workspace workspace = createWorkspace(10L);
        Workspace otherWorkspace = createWorkspace(99L);
        Board board = Board.builder().id(100L).name("Done").position(2).workspace(otherWorkspace).build();

        given(workspaceAccessService.getUserByEmail(email)).willReturn(user);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));

        assertThatThrownBy(() -> boardService.reorderBoard(email, 10L, 100L, new ReorderBoardRequest(1)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BOARD_WORKSPACE_MISMATCH);
    }

    private User createUser(Long id, String email) {
        return User.builder().id(id).email(email).password("pw").nickname("사용자").build();
    }

    private Workspace createWorkspace(Long id) {
        return Workspace.builder().id(id).name("백엔드").description("설명").inviteCode("AB12CD34").build();
    }
}

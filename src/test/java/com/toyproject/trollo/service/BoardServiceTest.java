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
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BoardService boardService;

    @Test
    @DisplayName("보드 생성 성공 시 워크스페이스 마지막 위치 + 1로 저장한다")
    void createBoardSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findMaxPositionByWorkspaceId(10L)).willReturn(3);
        given(boardRepository.save(any(Board.class))).willAnswer(invocation -> {
            Board board = invocation.getArgument(0);
            return Board.builder()
                    .id(100L)
                    .name(board.getName())
                    .position(board.getPosition())
                    .workspace(board.getWorkspace())
                    .build();
        });

        BoardResponse response = boardService.createBoard(ownerEmail, 10L, new CreateBoardRequest("진행중"));

        ArgumentCaptor<Board> boardCaptor = ArgumentCaptor.forClass(Board.class);
        verify(boardRepository).save(boardCaptor.capture());

        assertThat(boardCaptor.getValue().getPosition()).isEqualTo(4);
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.position()).isEqualTo(4);
        assertThat(response.workspaceId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("보드 생성 시 워크스페이스 소유자가 아니면 예외가 발생한다")
    void createBoardFailsWhenWorkspaceAccessDenied() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        User other = createUser(2L, "other@example.com");
        Workspace workspace = createWorkspace(10L, other);

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));

        assertThatThrownBy(() -> boardService.createBoard(ownerEmail, 10L, new CreateBoardRequest("진행중")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_ACCESS_DENIED);

        verify(boardRepository, never()).save(any(Board.class));
    }

    @Test
    @DisplayName("보드 순서 변경 성공 시 대상 위치로 이동하고 사이 보드들을 조정한다")
    void reorderBoardSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board board = Board.builder()
                .id(100L)
                .name("Done")
                .position(3)
                .workspace(workspace)
                .build();

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));
        given(boardRepository.findMaxPositionByWorkspaceId(10L)).willReturn(4);
        given(boardRepository.save(any(Board.class))).willAnswer(invocation -> invocation.getArgument(0));

        BoardResponse response = boardService.reorderBoard(ownerEmail, 10L, 100L, new ReorderBoardRequest(1));

        verify(boardRepository).flush();
        verify(boardRepository).shiftRight(10L, 3, 1);
        verify(boardRepository, never()).shiftLeft(10L, 3, 1);
        assertThat(response.position()).isEqualTo(1);
    }

    @Test
    @DisplayName("보드 순서 변경 시 위치가 범위를 벗어나면 예외가 발생한다")
    void reorderBoardFailsWhenPositionOutOfRange() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Board board = Board.builder()
                .id(100L)
                .name("Done")
                .position(3)
                .workspace(workspace)
                .build();

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));
        given(boardRepository.findMaxPositionByWorkspaceId(10L)).willReturn(4);

        assertThatThrownBy(() -> boardService.reorderBoard(ownerEmail, 10L, 100L, new ReorderBoardRequest(5)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BOARD_INVALID_POSITION);

        verify(boardRepository, never()).flush();
        verify(boardRepository, never()).save(any(Board.class));
    }

    @Test
    @DisplayName("보드 순서 변경 시 다른 워크스페이스 보드를 지정하면 예외가 발생한다")
    void reorderBoardFailsWhenWorkspaceMismatch() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace workspace = createWorkspace(10L, owner);
        Workspace otherWorkspace = createWorkspace(99L, owner);
        Board board = Board.builder()
                .id(100L)
                .name("Done")
                .position(3)
                .workspace(otherWorkspace)
                .build();

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(boardRepository.findById(100L)).willReturn(Optional.of(board));

        assertThatThrownBy(() -> boardService.reorderBoard(ownerEmail, 10L, 100L, new ReorderBoardRequest(1)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BOARD_WORKSPACE_MISMATCH);

        verify(boardRepository, never()).save(any(Board.class));
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
}

package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.workspace.CreateWorkspaceRequest;
import com.toyproject.trollo.dto.workspace.WorkspaceResponse;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    @Test
    @DisplayName("워크스페이스 생성 성공 시 생성된 워크스페이스 정보를 반환한다")
    void createWorkspaceSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = User.builder()
                .id(1L)
                .email(ownerEmail)
                .password("encoded-password")
                .nickname("소유자")
                .build();
        CreateWorkspaceRequest request = new CreateWorkspaceRequest("백엔드", "백엔드 작업 공간");

        Workspace savedWorkspace = Workspace.builder()
                .id(10L)
                .name(request.name())
                .description(request.description())
                .owner(owner)
                .build();

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.save(any(Workspace.class))).willReturn(savedWorkspace);

        WorkspaceResponse response = workspaceService.createWorkspace(ownerEmail, request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("백엔드");
        assertThat(response.description()).isEqualTo("백엔드 작업 공간");
        assertThat(response.ownerId()).isEqualTo(1L);
        assertThat(response.ownerEmail()).isEqualTo(ownerEmail);
        verify(workspaceRepository).save(any(Workspace.class));
    }

    @Test
    @DisplayName("워크스페이스 생성 시 사용자가 없으면 예외가 발생한다")
    void createWorkspaceFailsWhenOwnerNotFound() {
        String ownerEmail = "owner@example.com";
        CreateWorkspaceRequest request = new CreateWorkspaceRequest("백엔드", "백엔드 작업 공간");

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.createWorkspace(ownerEmail, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verifyNoInteractions(workspaceRepository);
    }

    @Test
    @DisplayName("워크스페이스 상세 조회 성공 시 본인 워크스페이스를 반환한다")
    void getWorkspaceByIdSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = User.builder()
                .id(1L)
                .email(ownerEmail)
                .password("encoded-password")
                .nickname("소유자")
                .build();
        Workspace workspace = Workspace.builder()
                .id(10L)
                .name("백엔드")
                .description("백엔드 작업 공간")
                .owner(owner)
                .build();

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));

        WorkspaceResponse response = workspaceService.getWorkspaceById(ownerEmail, 10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.ownerId()).isEqualTo(1L);
        assertThat(response.ownerEmail()).isEqualTo(ownerEmail);
    }

    @Test
    @DisplayName("워크스페이스 상세 조회 시 소유자가 다르면 예외가 발생한다")
    void getWorkspaceByIdFailsWhenOwnerMismatch() {
        String ownerEmail = "owner@example.com";
        User owner = User.builder()
                .id(1L)
                .email(ownerEmail)
                .password("encoded-password")
                .nickname("소유자")
                .build();
        User otherOwner = User.builder()
                .id(2L)
                .email("other@example.com")
                .password("encoded-password")
                .nickname("다른 사용자")
                .build();
        Workspace workspace = Workspace.builder()
                .id(10L)
                .name("백엔드")
                .description("백엔드 작업 공간")
                .owner(otherOwner)
                .build();

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));

        assertThatThrownBy(() -> workspaceService.getWorkspaceById(ownerEmail, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("내 워크스페이스 목록 조회 성공 시 소유한 워크스페이스 목록을 반환한다")
    void getMyWorkspacesSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = User.builder()
                .id(1L)
                .email(ownerEmail)
                .password("encoded-password")
                .nickname("소유자")
                .build();
        Workspace firstWorkspace = Workspace.builder()
                .id(20L)
                .name("프론트엔드")
                .description("프론트엔드 작업 공간")
                .owner(owner)
                .build();
        Workspace secondWorkspace = Workspace.builder()
                .id(10L)
                .name("백엔드")
                .description("백엔드 작업 공간")
                .owner(owner)
                .build();

        given(userRepository.findByEmail(ownerEmail)).willReturn(Optional.of(owner));
        given(workspaceRepository.findAllByOwnerIdOrderByIdDesc(1L))
                .willReturn(List.of(firstWorkspace, secondWorkspace));

        List<WorkspaceResponse> response = workspaceService.getMyWorkspaces(ownerEmail);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).id()).isEqualTo(20L);
        assertThat(response.get(1).id()).isEqualTo(10L);
    }
}

package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.workspace.CreateWorkspaceRequest;
import com.toyproject.trollo.dto.workspace.WorkspaceResponse;
import com.toyproject.trollo.entity.ActivityType;
import com.toyproject.trollo.entity.Membership;
import com.toyproject.trollo.entity.MembershipRole;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
import com.toyproject.trollo.repository.MembershipRepository;
import com.toyproject.trollo.repository.WorkspaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private WorkspaceAccessService workspaceAccessService;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private WorkspaceService workspaceService;

    @Test
    @DisplayName("워크스페이스 생성 성공 시 초대코드와 HOST 멤버십을 생성한다")
    void createWorkspaceSuccess() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);

        given(workspaceAccessService.getUserByEmail(ownerEmail)).willReturn(owner);
        given(workspaceRepository.findByInviteCode(any(String.class))).willReturn(Optional.empty());
        given(workspaceRepository.save(any(Workspace.class))).willAnswer(invocation -> {
            Workspace workspace = invocation.getArgument(0);
            return Workspace.builder()
                    .id(10L)
                    .name(workspace.getName())
                    .description(workspace.getDescription())
                    .inviteCode(workspace.getInviteCode())
                    .build();
        });

        WorkspaceResponse response = workspaceService.createWorkspace(
                ownerEmail,
                new CreateWorkspaceRequest("백엔드", "백엔드 작업 공간")
        );

        ArgumentCaptor<Membership> membershipCaptor = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(membershipCaptor.capture());
        verify(activityLogService).log(any(Workspace.class), any(User.class), any(ActivityType.class), any(String.class));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.inviteCode()).hasSize(8);
        assertThat(membershipCaptor.getValue().getRole()).isEqualTo(MembershipRole.HOST);
        assertThat(membershipCaptor.getValue().getUser()).isEqualTo(owner);
    }

    @Test
    @DisplayName("워크스페이스 상세 조회 시 멤버가 아니면 예외가 발생한다")
    void getWorkspaceByIdFailsWhenAccessDenied() {
        String userEmail = "user@example.com";
        User user = createUser(1L, userEmail);

        given(workspaceAccessService.getUserByEmail(userEmail)).willReturn(user);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(createWorkspace(10L, "AB12CD34"));
        given(workspaceAccessService.getMembership(10L, 1L))
                .willThrow(new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        assertThatThrownBy(() -> workspaceService.getWorkspaceById(userEmail, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("내 워크스페이스 목록 조회 시 멤버십 기준으로 반환한다")
    void getMyWorkspacesSuccess() {
        String userEmail = "user@example.com";
        User user = createUser(1L, userEmail);
        Workspace first = createWorkspace(20L, "ZXCV1234");
        Workspace second = createWorkspace(10L, "QWER5678");

        given(workspaceAccessService.getUserByEmail(userEmail)).willReturn(user);
        given(membershipRepository.findAllByUserIdOrderByWorkspaceIdDesc(1L)).willReturn(List.of(
                Membership.builder().workspace(first).user(user).role(MembershipRole.HOST).build(),
                Membership.builder().workspace(second).user(user).role(MembershipRole.MEMBER).build()
        ));

        List<WorkspaceResponse> response = workspaceService.getMyWorkspaces(userEmail);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).id()).isEqualTo(20L);
        assertThat(response.get(1).id()).isEqualTo(10L);
    }

    @Test
    @DisplayName("워크스페이스 삭제 시 HOST 권한을 검증한다")
    void deleteWorkspaceValidatesHostRole() {
        String userEmail = "user@example.com";
        User user = createUser(1L, userEmail);
        Workspace workspace = createWorkspace(10L, "AB12CD34");

        given(workspaceAccessService.getUserByEmail(userEmail)).willReturn(user);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);

        workspaceService.deleteWorkspace(userEmail, 10L);

        verify(workspaceAccessService).requireHost(10L, 1L);
        verify(workspaceRepository).delete(workspace);
    }

    private User createUser(Long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .password("encoded-password")
                .nickname("사용자")
                .build();
    }

    private Workspace createWorkspace(Long id, String inviteCode) {
        return Workspace.builder()
                .id(id)
                .name("백엔드")
                .description("백엔드 작업 공간")
                .inviteCode(inviteCode)
                .build();
    }
}

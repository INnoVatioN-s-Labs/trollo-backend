package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.workspace.CreateWorkspaceRequest;
import com.toyproject.trollo.dto.workspace.JoinWorkspaceRequest;
import com.toyproject.trollo.dto.workspace.JoinWorkspaceResponse;
import com.toyproject.trollo.dto.workspace.TransferHostRequest;
import com.toyproject.trollo.dto.workspace.TransferHostResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
        verify(activityLogService).saveLog(any(Workspace.class), any(User.class), any(ActivityType.class), any(String.class));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.inviteCode()).hasSize(8);
        assertThat(response.memberCount()).isEqualTo(1L);
        assertThat(membershipCaptor.getValue().getRole()).isEqualTo(MembershipRole.HOST);
        assertThat(membershipCaptor.getValue().getUser()).isEqualTo(owner);
    }

    @Test
    @DisplayName("워크스페이스 생성 시 초대코드가 중복되면 재시도 후 생성한다")
    void createWorkspaceRetriesWhenInviteCodeDuplicated() {
        String ownerEmail = "owner@example.com";
        User owner = createUser(1L, ownerEmail);
        Workspace duplicated = createWorkspace(99L, "DUPL1234");

        given(workspaceAccessService.getUserByEmail(ownerEmail)).willReturn(owner);
        given(workspaceRepository.findByInviteCode(any(String.class)))
                .willReturn(Optional.of(duplicated))
                .willReturn(Optional.empty());
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

        assertThat(response.inviteCode()).hasSize(8);
        assertThat(response.memberCount()).isEqualTo(1L);
        verify(workspaceRepository).save(any(Workspace.class));
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
        given(membershipRepository.countByWorkspaceId(20L)).willReturn(4L);
        given(membershipRepository.countByWorkspaceId(10L)).willReturn(2L);

        List<WorkspaceResponse> response = workspaceService.getMyWorkspaces(userEmail);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).id()).isEqualTo(20L);
        assertThat(response.get(0).memberCount()).isEqualTo(4L);
        assertThat(response.get(1).id()).isEqualTo(10L);
        assertThat(response.get(1).memberCount()).isEqualTo(2L);
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

    @Test
    @DisplayName("초대 코드 참여 성공 시 MEMBER 멤버십을 생성한다")
    void joinWorkspaceSuccess() {
        String userEmail = "member@example.com";
        User user = createUser(2L, userEmail);
        Workspace workspace = createWorkspace(10L, "AB12CD34");
        LocalDateTime joinedAt = LocalDateTime.of(2026, 3, 8, 16, 0, 0);

        given(workspaceAccessService.getUserByEmail(userEmail)).willReturn(user);
        given(workspaceRepository.findByInviteCode("AB12CD34")).willReturn(Optional.of(workspace));
        given(membershipRepository.existsByWorkspaceIdAndUserId(10L, 2L)).willReturn(false);
        given(membershipRepository.countByWorkspaceId(10L)).willReturn(3L);
        given(membershipRepository.save(any(Membership.class))).willAnswer(invocation -> {
            Membership membership = invocation.getArgument(0);
            return Membership.builder()
                    .id(100L)
                    .workspace(membership.getWorkspace())
                    .user(membership.getUser())
                    .role(membership.getRole())
                    .joinedAt(joinedAt)
                    .build();
        });

        JoinWorkspaceResponse response = workspaceService.joinWorkspace(userEmail, new JoinWorkspaceRequest("AB12CD34"));

        assertThat(response.workspaceId()).isEqualTo(10L);
        assertThat(response.role()).isEqualTo(MembershipRole.MEMBER);
        assertThat(response.joinedAt()).isEqualTo(joinedAt);
        verify(activityLogService).saveLog(workspace, user, ActivityType.MEMBER_JOIN, "워크스페이스에 참여했습니다.");
    }

    @Test
    @DisplayName("초대 코드 참여 시 이미 멤버면 예외가 발생한다")
    void joinWorkspaceFailsWhenAlreadyMember() {
        String userEmail = "member@example.com";
        User user = createUser(2L, userEmail);
        Workspace workspace = createWorkspace(10L, "AB12CD34");

        given(workspaceAccessService.getUserByEmail(userEmail)).willReturn(user);
        given(workspaceRepository.findByInviteCode("AB12CD34")).willReturn(Optional.of(workspace));
        given(membershipRepository.existsByWorkspaceIdAndUserId(10L, 2L)).willReturn(true);

        assertThatThrownBy(() -> workspaceService.joinWorkspace(userEmail, new JoinWorkspaceRequest("AB12CD34")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_MEMBER_ALREADY_EXISTS);

        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test
    @DisplayName("초대 코드 참여 시 코드가 유효하지 않으면 예외가 발생한다")
    void joinWorkspaceFailsWhenInviteCodeInvalid() {
        String userEmail = "member@example.com";
        User user = createUser(2L, userEmail);

        given(workspaceAccessService.getUserByEmail(userEmail)).willReturn(user);
        given(workspaceRepository.findByInviteCode("INVALID1")).willReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.joinWorkspace(userEmail, new JoinWorkspaceRequest("INVALID1")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_INVITE_CODE_INVALID);

        verifyNoInteractions(activityLogService);
    }

    @Test
    @DisplayName("초대 코드 참여 시 멤버 수가 10명 이상이면 예외가 발생한다")
    void joinWorkspaceFailsWhenMemberLimitExceeded() {
        String userEmail = "member@example.com";
        User user = createUser(2L, userEmail);
        Workspace workspace = createWorkspace(10L, "AB12CD34");

        given(workspaceAccessService.getUserByEmail(userEmail)).willReturn(user);
        given(workspaceRepository.findByInviteCode("AB12CD34")).willReturn(Optional.of(workspace));
        given(membershipRepository.existsByWorkspaceIdAndUserId(10L, 2L)).willReturn(false);
        given(membershipRepository.countByWorkspaceId(10L)).willReturn(10L);

        assertThatThrownBy(() -> workspaceService.joinWorkspace(userEmail, new JoinWorkspaceRequest("AB12CD34")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_MEMBER_LIMIT_EXCEEDED);

        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test
    @DisplayName("멤버 강퇴 성공 시 대상 멤버십을 삭제한다")
    void removeMemberSuccess() {
        String hostEmail = "host@example.com";
        User host = createUser(1L, hostEmail);
        Workspace workspace = createWorkspace(10L, "AB12CD34");
        Membership target = Membership.builder()
                .id(101L)
                .workspace(workspace)
                .user(createUser(2L, "member@example.com"))
                .role(MembershipRole.MEMBER)
                .build();

        given(workspaceAccessService.getUserByEmail(hostEmail)).willReturn(host);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);
        given(membershipRepository.findByWorkspaceIdAndUserId(10L, 2L)).willReturn(Optional.of(target));

        workspaceService.removeMember(hostEmail, 10L, 2L);

        verify(workspaceAccessService).requireHost(10L, 1L);
        verify(membershipRepository).delete(target);
        verify(activityLogService).saveLog(workspace, host, ActivityType.MEMBER_REMOVE, "멤버를 강퇴했습니다. userId=2");
    }

    @Test
    @DisplayName("멤버 강퇴 시 자기 자신을 대상으로 하면 예외가 발생한다")
    void removeMemberFailsWhenSelfTargeted() {
        String hostEmail = "host@example.com";
        User host = createUser(1L, hostEmail);
        Workspace workspace = createWorkspace(10L, "AB12CD34");

        given(workspaceAccessService.getUserByEmail(hostEmail)).willReturn(host);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);

        assertThatThrownBy(() -> workspaceService.removeMember(hostEmail, 10L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_HOST_ONLY);
    }

    @Test
    @DisplayName("멤버 강퇴 시 대상 멤버가 없으면 예외가 발생한다")
    void removeMemberFailsWhenTargetNotFound() {
        String hostEmail = "host@example.com";
        User host = createUser(1L, hostEmail);
        Workspace workspace = createWorkspace(10L, "AB12CD34");

        given(workspaceAccessService.getUserByEmail(hostEmail)).willReturn(host);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);
        given(membershipRepository.findByWorkspaceIdAndUserId(10L, 3L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.removeMember(hostEmail, 10L, 3L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("멤버 강퇴 시 대상이 HOST면 예외가 발생한다")
    void removeMemberFailsWhenTargetIsHost() {
        String hostEmail = "host@example.com";
        User host = createUser(1L, hostEmail);
        Workspace workspace = createWorkspace(10L, "AB12CD34");
        Membership target = Membership.builder()
                .id(102L)
                .workspace(workspace)
                .user(createUser(2L, "another-host@example.com"))
                .role(MembershipRole.HOST)
                .build();

        given(workspaceAccessService.getUserByEmail(hostEmail)).willReturn(host);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);
        given(membershipRepository.findByWorkspaceIdAndUserId(10L, 2L)).willReturn(Optional.of(target));

        assertThatThrownBy(() -> workspaceService.removeMember(hostEmail, 10L, 2L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_HOST_ONLY);
    }

    @Test
    @DisplayName("호스트 양도 성공 시 기존 호스트는 MEMBER, 대상은 HOST로 변경한다")
    void transferHostSuccess() {
        String hostEmail = "host@example.com";
        User host = createUser(1L, hostEmail);
        Workspace workspace = createWorkspace(10L, "AB12CD34");
        Membership hostMembership = Membership.builder()
                .id(100L)
                .workspace(workspace)
                .user(host)
                .role(MembershipRole.HOST)
                .build();
        Membership targetMembership = Membership.builder()
                .id(101L)
                .workspace(workspace)
                .user(createUser(2L, "member@example.com"))
                .role(MembershipRole.MEMBER)
                .build();

        given(workspaceAccessService.getUserByEmail(hostEmail)).willReturn(host);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);
        given(workspaceAccessService.requireHost(10L, 1L)).willReturn(hostMembership);
        given(membershipRepository.findByWorkspaceIdAndUserId(10L, 2L)).willReturn(Optional.of(targetMembership));
        given(membershipRepository.save(any(Membership.class))).willAnswer(invocation -> invocation.getArgument(0));

        TransferHostResponse response = workspaceService.transferHost(hostEmail, 10L, new TransferHostRequest(2L));

        assertThat(response.workspaceId()).isEqualTo(10L);
        assertThat(response.previousHostUserId()).isEqualTo(1L);
        assertThat(response.newHostUserId()).isEqualTo(2L);
        assertThat(hostMembership.getRole()).isEqualTo(MembershipRole.MEMBER);
        assertThat(targetMembership.getRole()).isEqualTo(MembershipRole.HOST);
        verify(activityLogService).saveLog(workspace, host, ActivityType.HOST_TRANSFER, "호스트를 양도했습니다. from=1, to=2");
    }

    @Test
    @DisplayName("호스트 양도 시 대상 멤버가 없으면 예외가 발생한다")
    void transferHostFailsWhenTargetNotFound() {
        String hostEmail = "host@example.com";
        User host = createUser(1L, hostEmail);
        Workspace workspace = createWorkspace(10L, "AB12CD34");
        Membership hostMembership = Membership.builder()
                .id(100L)
                .workspace(workspace)
                .user(host)
                .role(MembershipRole.HOST)
                .build();

        given(workspaceAccessService.getUserByEmail(hostEmail)).willReturn(host);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);
        given(workspaceAccessService.requireHost(10L, 1L)).willReturn(hostMembership);
        given(membershipRepository.findByWorkspaceIdAndUserId(10L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.transferHost(hostEmail, 10L, new TransferHostRequest(2L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("호스트 양도 시 대상이 이미 HOST면 예외가 발생한다")
    void transferHostFailsWhenTargetAlreadyHost() {
        String hostEmail = "host@example.com";
        User host = createUser(1L, hostEmail);
        Workspace workspace = createWorkspace(10L, "AB12CD34");
        Membership hostMembership = Membership.builder()
                .id(100L)
                .workspace(workspace)
                .user(host)
                .role(MembershipRole.HOST)
                .build();
        Membership targetMembership = Membership.builder()
                .id(101L)
                .workspace(workspace)
                .user(createUser(2L, "host2@example.com"))
                .role(MembershipRole.HOST)
                .build();

        given(workspaceAccessService.getUserByEmail(hostEmail)).willReturn(host);
        given(workspaceAccessService.getWorkspace(10L)).willReturn(workspace);
        given(workspaceAccessService.requireHost(10L, 1L)).willReturn(hostMembership);
        given(membershipRepository.findByWorkspaceIdAndUserId(10L, 2L)).willReturn(Optional.of(targetMembership));

        assertThatThrownBy(() -> workspaceService.transferHost(hostEmail, 10L, new TransferHostRequest(2L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_HOST_ONLY);

        verify(membershipRepository, never()).save(any(Membership.class));
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

package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.util.InviteCodeGenerator;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final MembershipRepository membershipRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final ActivityLogService activityLogService;

    @Transactional
    public WorkspaceResponse createWorkspace(String ownerEmail, CreateWorkspaceRequest request) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);

        Workspace workspace = Workspace.builder()
                .name(request.name())
                .description(request.description())
                .inviteCode(generateUniqueInviteCode())
                .build();

        Workspace savedWorkspace = workspaceRepository.save(workspace);
        membershipRepository.save(Membership.builder()
                .workspace(savedWorkspace)
                .user(owner)
                .role(MembershipRole.HOST)
                .build());
        activityLogService.saveLog(savedWorkspace, owner, ActivityType.WORKSPACE_CREATE, "워크스페이스를 생성했습니다.");
        return toResponse(savedWorkspace, 1L);
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(String ownerEmail, Long workspaceId) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        Workspace workspace = workspaceAccessService.getWorkspace(workspaceId);
        workspaceAccessService.getMembership(workspaceId, owner.getId());
        return toResponse(workspace);
    }

    @Transactional
    public void deleteWorkspace(String ownerEmail, Long workspaceId) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        Workspace workspace = workspaceAccessService.getWorkspace(workspaceId);
        workspaceAccessService.requireHost(workspaceId, owner.getId());
        activityLogService.saveLog(workspace, owner, ActivityType.WORKSPACE_DELETE, "워크스페이스를 삭제했습니다.");
        workspaceRepository.delete(workspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getMyWorkspaces(String ownerEmail) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        return membershipRepository.findAllByUserIdOrderByWorkspaceIdDesc(owner.getId())
                .stream()
                .map(Membership::getWorkspace)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public JoinWorkspaceResponse joinWorkspace(String userEmail, JoinWorkspaceRequest request) {
        User user = workspaceAccessService.getUserByEmail(userEmail);
        Workspace workspace = workspaceRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_INVITE_CODE_INVALID));

        if (membershipRepository.existsByWorkspaceIdAndUserId(workspace.getId(), user.getId())) {
            throw new BusinessException(ErrorCode.WORKSPACE_MEMBER_ALREADY_EXISTS);
        }

        long memberCount = membershipRepository.countByWorkspaceId(workspace.getId());
        if (memberCount >= 10) {
            throw new BusinessException(ErrorCode.WORKSPACE_MEMBER_LIMIT_EXCEEDED);
        }

        Membership membership = membershipRepository.save(Membership.builder()
                .workspace(workspace)
                .user(user)
                .role(MembershipRole.MEMBER)
                .build());

        activityLogService.saveLog(workspace, user, ActivityType.MEMBER_JOIN, "워크스페이스에 참여했습니다.");

        return new JoinWorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                membership.getRole(),
                membership.getJoinedAt() != null ? membership.getJoinedAt() : LocalDateTime.now()
        );
    }

    @Transactional
    public void removeMember(String requesterEmail, Long workspaceId, Long targetUserId) {
        User requester = workspaceAccessService.getUserByEmail(requesterEmail);
        Workspace workspace = workspaceAccessService.getWorkspace(workspaceId);
        workspaceAccessService.requireHost(workspaceId, requester.getId());

        if (requester.getId().equals(targetUserId)) {
            throw new BusinessException(ErrorCode.WORKSPACE_HOST_ONLY, "호스트는 자기 자신을 강퇴할 수 없습니다.");
        }

        Membership targetMembership = membershipRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_MEMBER_NOT_FOUND));

        if (targetMembership.getRole() == MembershipRole.HOST) {
            throw new BusinessException(ErrorCode.WORKSPACE_HOST_ONLY, "호스트는 강퇴할 수 없습니다. 먼저 호스트를 양도하세요.");
        }

        membershipRepository.delete(targetMembership);
        activityLogService.saveLog(workspace, requester, ActivityType.MEMBER_REMOVE, "멤버를 강퇴했습니다. userId=" + targetUserId);
    }

    @Transactional
    public TransferHostResponse transferHost(String requesterEmail, Long workspaceId, TransferHostRequest request) {
        User requester = workspaceAccessService.getUserByEmail(requesterEmail);
        Workspace workspace = workspaceAccessService.getWorkspace(workspaceId);
        Membership requesterMembership = workspaceAccessService.requireHost(workspaceId, requester.getId());

        Membership targetMembership = membershipRepository.findByWorkspaceIdAndUserId(workspaceId, request.targetUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_MEMBER_NOT_FOUND));

        if (targetMembership.getRole() == MembershipRole.HOST) {
            throw new BusinessException(ErrorCode.WORKSPACE_HOST_ONLY, "이미 호스트인 사용자입니다.");
        }

        requesterMembership.updateRole(MembershipRole.MEMBER);
        targetMembership.updateRole(MembershipRole.HOST);

        membershipRepository.save(requesterMembership);
        membershipRepository.save(targetMembership);

        activityLogService.saveLog(
                workspace,
                requester,
                ActivityType.HOST_TRANSFER,
                "호스트를 양도했습니다. from=" + requester.getId() + ", to=" + request.targetUserId()
        );

        return new TransferHostResponse(workspaceId, requester.getId(), request.targetUserId());
    }

    private String generateUniqueInviteCode() {
        int tryCount = 0;
        while (tryCount < 10) {
            String code = InviteCodeGenerator.generate();
            if (workspaceRepository.findByInviteCode(code).isEmpty()) {
                return code;
            }
            tryCount++;
        }
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "초대 코드 생성에 실패했습니다.");
    }

    private WorkspaceResponse toResponse(Workspace workspace) {
        long memberCount = membershipRepository.countByWorkspaceId(workspace.getId());
        return toResponse(workspace, memberCount);
    }

    private WorkspaceResponse toResponse(Workspace workspace, long memberCount) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getInviteCode(),
                memberCount
        );
    }

}

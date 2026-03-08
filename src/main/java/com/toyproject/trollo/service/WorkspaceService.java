package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.util.InviteCodeGenerator;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        activityLogService.log(savedWorkspace, owner, ActivityType.WORKSPACE_CREATE, "워크스페이스를 생성했습니다.");
        return toResponse(savedWorkspace);
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
        activityLogService.log(workspace, owner, ActivityType.WORKSPACE_DELETE, "워크스페이스를 삭제했습니다.");
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
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getInviteCode()
        );
    }

}

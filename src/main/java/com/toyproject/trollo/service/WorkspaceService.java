package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.workspace.CreateWorkspaceRequest;
import com.toyproject.trollo.dto.workspace.WorkspaceResponse;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
import com.toyproject.trollo.repository.UserRepository;
import com.toyproject.trollo.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Transactional
    public WorkspaceResponse createWorkspace(String ownerEmail, CreateWorkspaceRequest request) {
        User owner = getUserByEmail(ownerEmail);

        Workspace workspace = Workspace.builder()
                .name(request.name())
                .description(request.description())
                .owner(owner)
                .build();

        Workspace savedWorkspace = workspaceRepository.save(workspace);
        return toResponse(savedWorkspace);
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(String ownerEmail, Long workspaceId) {
        User owner = getUserByEmail(ownerEmail);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (!workspace.getOwner().getId().equals(owner.getId())) {
            throw new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        return toResponse(workspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getMyWorkspaces(String ownerEmail) {
        User owner = getUserByEmail(ownerEmail);
        return workspaceRepository.findAllByOwnerIdOrderByIdDesc(owner.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private User getUserByEmail(String ownerEmail) {
        return userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private WorkspaceResponse toResponse(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getOwner().getId(),
                workspace.getOwner().getEmail()
        );
    }

}

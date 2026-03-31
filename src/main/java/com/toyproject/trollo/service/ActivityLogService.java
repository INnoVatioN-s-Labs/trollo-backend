package com.toyproject.trollo.service;

import com.toyproject.trollo.dto.activity.ActivityLogResponse;
import com.toyproject.trollo.entity.ActivityLog;
import com.toyproject.trollo.entity.ActivityType;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
import com.toyproject.trollo.repository.ActivityLogRepository;
import com.toyproject.trollo.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final MembershipRepository membershipRepository;

    @Transactional
    public void saveLog(Workspace workspace, User user, ActivityType type, String content) {
        saveLog(workspace, user, null, type, content);
    }

    @Transactional
    public void saveLog(Workspace workspace, User user, com.toyproject.trollo.entity.Ticket ticket, ActivityType type, String content) {
        activityLogRepository.save(ActivityLog.builder()
                .workspace(workspace)
                .user(user)
                .ticket(ticket)
                .type(type)
                .content(content)
                .build());
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getRecentActivities(String userEmail, Long workspaceId) {
        User user = workspaceAccessService.getUserByEmail(userEmail);
        workspaceAccessService.getMembership(workspaceId, user.getId());

        return activityLogRepository.findTop10ByWorkspaceIdOrderByCreatedAtDesc(workspaceId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getMyRecentActivities(String userEmail) {
        User user = workspaceAccessService.getUserByEmail(userEmail);
        List<Long> workspaceIds = membershipRepository.findAllByUserIdOrderByWorkspaceIdDesc(user.getId())
                .stream()
                .map(membership -> membership.getWorkspace().getId())
                .toList();

        if (workspaceIds.isEmpty()) {
            return List.of();
        }

        return activityLogRepository.findTop10ByWorkspaceIdInOrderByCreatedAtDesc(workspaceIds)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ActivityLogResponse mapToResponse(ActivityLog activity) {
        return new ActivityLogResponse(
                activity.getId(),
                activity.getType(),
                activity.getContent(),
                activity.getUser().getId(),
                activity.getUser().getNickname(),
                activity.getCreatedAt(),
                activity.getWorkspace().getId(),
                activity.getWorkspace().getName()
        );
    }
}

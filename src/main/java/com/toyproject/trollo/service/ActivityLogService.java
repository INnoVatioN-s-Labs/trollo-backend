package com.toyproject.trollo.service;

import com.toyproject.trollo.dto.activity.ActivityLogResponse;
import com.toyproject.trollo.entity.ActivityLog;
import com.toyproject.trollo.entity.ActivityType;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
import com.toyproject.trollo.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final WorkspaceAccessService workspaceAccessService;

    @Transactional
    public void saveLog(Workspace workspace, User user, ActivityType type, String content) {
        activityLogRepository.save(ActivityLog.builder()
                .workspace(workspace)
                .user(user)
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
                .map(activity -> new ActivityLogResponse(
                        activity.getId(),
                        activity.getType(),
                        activity.getContent(),
                        activity.getUser().getId(),
                        activity.getUser().getNickname(),
                        activity.getCreatedAt()
                ))
                .toList();
    }
}

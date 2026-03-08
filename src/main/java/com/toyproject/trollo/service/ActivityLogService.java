package com.toyproject.trollo.service;

import com.toyproject.trollo.entity.ActivityLog;
import com.toyproject.trollo.entity.ActivityType;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
import com.toyproject.trollo.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void log(Workspace workspace, User user, ActivityType type, String content) {
        activityLogRepository.save(ActivityLog.builder()
                .workspace(workspace)
                .user(user)
                .type(type)
                .content(content)
                .build());
    }
}

package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.activity.ActivityLogResponse;
import com.toyproject.trollo.entity.ActivityLog;
import com.toyproject.trollo.entity.ActivityType;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
import com.toyproject.trollo.repository.ActivityLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private WorkspaceAccessService workspaceAccessService;

    @InjectMocks
    private ActivityLogService activityLogService;

    @Test
    @DisplayName("활동 로그 저장 시 리포지토리에 저장된다")
    void saveLogSuccess() {
        Workspace workspace = createWorkspace(10L);
        User user = createUser(1L, "user@example.com");

        activityLogService.saveLog(workspace, user, ActivityType.TICKET_CREATE, "티켓을 생성했습니다.");

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository).save(captor.capture());
        assertThat(captor.getValue().getWorkspace().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getUser().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getType()).isEqualTo(ActivityType.TICKET_CREATE);
        assertThat(captor.getValue().getContent()).isEqualTo("티켓을 생성했습니다.");
    }

    @Test
    @DisplayName("최근 활동 조회 성공 시 최신순 10건을 DTO로 반환한다")
    void getRecentActivitiesSuccess() {
        String email = "user@example.com";
        User requester = createUser(1L, email);
        Workspace workspace = createWorkspace(10L);
        User actor = createUser(2L, "actor@example.com");

        ActivityLog first = ActivityLog.builder()
                .id(101L)
                .workspace(workspace)
                .user(actor)
                .type(ActivityType.TICKET_MOVE)
                .content("티켓을 이동했습니다.")
                .build();
        ActivityLog second = ActivityLog.builder()
                .id(100L)
                .workspace(workspace)
                .user(actor)
                .type(ActivityType.BOARD_CREATE)
                .content("보드를 생성했습니다.")
                .build();

        given(workspaceAccessService.getUserByEmail(email)).willReturn(requester);
        given(activityLogRepository.findTop10ByWorkspaceIdOrderByCreatedAtDesc(10L)).willReturn(List.of(first, second));

        List<ActivityLogResponse> responses = activityLogService.getRecentActivities(email, 10L);

        verify(workspaceAccessService).getMembership(10L, 1L);
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(101L);
        assertThat(responses.get(0).type()).isEqualTo(ActivityType.TICKET_MOVE);
        assertThat(responses.get(0).userId()).isEqualTo(2L);
        assertThat(responses.get(1).id()).isEqualTo(100L);
    }

    @Test
    @DisplayName("최근 활동 조회 시 워크스페이스 멤버가 아니면 예외가 발생한다")
    void getRecentActivitiesFailsWhenAccessDenied() {
        String email = "user@example.com";
        User requester = createUser(1L, email);

        given(workspaceAccessService.getUserByEmail(email)).willReturn(requester);
        given(workspaceAccessService.getMembership(10L, 1L))
                .willThrow(new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        assertThatThrownBy(() -> activityLogService.getRecentActivities(email, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WORKSPACE_ACCESS_DENIED);

        verifyNoInteractions(activityLogRepository);
    }

    private User createUser(Long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .password("pw")
                .nickname("사용자")
                .build();
    }

    private Workspace createWorkspace(Long id) {
        return Workspace.builder()
                .id(id)
                .name("워크스페이스")
                .description("설명")
                .inviteCode("AB12CD34")
                .build();
    }
}

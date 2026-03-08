package com.toyproject.trollo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyproject.trollo.dto.activity.ActivityLogResponse;
import com.toyproject.trollo.dto.workspace.JoinWorkspaceRequest;
import com.toyproject.trollo.dto.workspace.JoinWorkspaceResponse;
import com.toyproject.trollo.dto.workspace.TransferHostRequest;
import com.toyproject.trollo.dto.workspace.TransferHostResponse;
import com.toyproject.trollo.entity.ActivityType;
import com.toyproject.trollo.entity.MembershipRole;
import com.toyproject.trollo.security.JwtAuthenticationFilter;
import com.toyproject.trollo.service.ActivityLogService;
import com.toyproject.trollo.service.WorkspaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkspaceService workspaceService;

    @MockBean
    private ActivityLogService activityLogService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("초대 코드 참여 API 성공 시 참여 정보를 반환한다")
    @WithMockUser(username = "user@example.com")
    void joinWorkspaceSuccess() throws Exception {
        String email = "user@example.com";
        JoinWorkspaceResponse response = new JoinWorkspaceResponse(
                10L, "백엔드", MembershipRole.MEMBER, LocalDateTime.of(2026, 3, 8, 16, 40, 0)
        );
        given(workspaceService.joinWorkspace(eq(email), eq(new JoinWorkspaceRequest("AB12CD34"))))
                .willReturn(response);

        mockMvc.perform(post("/api/workspaces/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JoinWorkspaceRequest("AB12CD34"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("0000"))
                .andExpect(jsonPath("$.data.workspaceId").value(10))
                .andExpect(jsonPath("$.data.workspaceName").value("백엔드"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"));
    }

    @Test
    @DisplayName("초대 코드 참여 API 요청값이 유효하지 않으면 검증 실패를 반환한다")
    @WithMockUser(username = "user@example.com")
    void joinWorkspaceValidationFail() throws Exception {
        mockMvc.perform(post("/api/workspaces/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inviteCode\":\"ab1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("-8404"));
    }

    @Test
    @DisplayName("멤버 강퇴 API 성공 시 성공 응답을 반환한다")
    @WithMockUser(username = "host@example.com")
    void removeMemberSuccess() throws Exception {
        mockMvc.perform(delete("/api/workspaces/10/members/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("0000"));
    }

    @Test
    @DisplayName("호스트 양도 API 성공 시 양도 결과를 반환한다")
    @WithMockUser(username = "host@example.com")
    void transferHostSuccess() throws Exception {
        String email = "host@example.com";
        TransferHostResponse response = new TransferHostResponse(10L, 1L, 2L);
        given(workspaceService.transferHost(eq(email), eq(10L), eq(new TransferHostRequest(2L))))
                .willReturn(response);

        mockMvc.perform(patch("/api/workspaces/10/transfer-host")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransferHostRequest(2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("0000"))
                .andExpect(jsonPath("$.data.workspaceId").value(10))
                .andExpect(jsonPath("$.data.previousHostUserId").value(1))
                .andExpect(jsonPath("$.data.newHostUserId").value(2));
    }

    @Test
    @DisplayName("최근 활동 조회 API 성공 시 최신 활동 목록을 반환한다")
    @WithMockUser(username = "user@example.com")
    void getRecentActivitiesSuccess() throws Exception {
        String email = "user@example.com";
        ActivityLogResponse activity = new ActivityLogResponse(
                101L,
                ActivityType.TICKET_MOVE,
                "티켓을 이동했습니다: API 구현",
                2L,
                "개발자",
                LocalDateTime.of(2026, 3, 8, 16, 50, 0)
        );
        given(activityLogService.getRecentActivities(email, 10L)).willReturn(List.of(activity));

        mockMvc.perform(get("/api/workspaces/10/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("0000"))
                .andExpect(jsonPath("$.data[0].id").value(101))
                .andExpect(jsonPath("$.data[0].type").value("TICKET_MOVE"))
                .andExpect(jsonPath("$.data[0].userNickname").value("개발자"));
    }
}

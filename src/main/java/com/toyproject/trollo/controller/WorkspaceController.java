package com.toyproject.trollo.controller;

import com.toyproject.trollo.common.util.ReturnMessage;
import com.toyproject.trollo.dto.activity.ActivityLogResponse;
import com.toyproject.trollo.dto.workspace.CreateWorkspaceRequest;
import com.toyproject.trollo.dto.workspace.JoinWorkspaceRequest;
import com.toyproject.trollo.dto.workspace.JoinWorkspaceResponse;
import com.toyproject.trollo.dto.workspace.TransferHostRequest;
import com.toyproject.trollo.dto.workspace.TransferHostResponse;
import com.toyproject.trollo.dto.workspace.WorkspaceResponse;
import com.toyproject.trollo.service.ActivityLogService;
import com.toyproject.trollo.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspace", description = "워크스페이스 API")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final ActivityLogService activityLogService;

    @PostMapping
    @Operation(summary = "워크스페이스 생성", description = "현재 로그인한 사용자의 워크스페이스를 생성합니다.")
    public ReturnMessage<WorkspaceResponse> createWorkspace(
            @AuthenticationPrincipal UserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "워크스페이스 생성 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "워크스페이스 생성 예시",
                                    value = "{\"name\":\"백엔드\",\"description\":\"백엔드 작업 공간\"}"
                            )
                    )
            )
            @Valid @RequestBody CreateWorkspaceRequest request
    ) {
        WorkspaceResponse response = workspaceService.createWorkspace(userDetails.getUsername(), request);
        return new ReturnMessage<>(response);
    }

    @GetMapping("/{workspaceId}")
    @Operation(summary = "워크스페이스 단건 조회", description = "워크스페이스 ID로 상세 정보를 조회합니다.")
    public ReturnMessage<WorkspaceResponse> getWorkspaceById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId
    ) {
        WorkspaceResponse response = workspaceService.getWorkspaceById(userDetails.getUsername(), workspaceId);
        return new ReturnMessage<>(response);
    }

    @GetMapping
    @Operation(summary = "내 워크스페이스 목록 조회", description = "현재 로그인한 사용자가 소유한 워크스페이스 목록을 조회합니다.")
    public ReturnMessage<List<WorkspaceResponse>> getMyWorkspaces(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<WorkspaceResponse> response = workspaceService.getMyWorkspaces(userDetails.getUsername());
        return new ReturnMessage<>(response);
    }

    @GetMapping("/{workspaceId}/activities")
    @Operation(summary = "최근 활동 조회", description = "워크스페이스 최근 활동 이력을 최신순 10건 조회합니다.")
    public ReturnMessage<List<ActivityLogResponse>> getRecentActivities(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId
    ) {
        List<ActivityLogResponse> response = activityLogService.getRecentActivities(userDetails.getUsername(), workspaceId);
        return new ReturnMessage<>(response);
    }

    @GetMapping("/activities")
    @Operation(summary = "전체 최근 활동 통합 조회", description = "사용자가 속한 모든 워크스페이스의 최근 활동을 통합하여 최신순으로 10건 조회합니다.")
    public ReturnMessage<List<ActivityLogResponse>> getMyRecentActivities(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ActivityLogResponse> response = activityLogService.getMyRecentActivities(userDetails.getUsername());
        return new ReturnMessage<>(response);
    }

    @PostMapping("/join")
    @Operation(summary = "초대 코드로 워크스페이스 참여", description = "유효한 초대 코드를 통해 워크스페이스에 멤버로 참여합니다.")
    public ReturnMessage<JoinWorkspaceResponse> joinWorkspace(
            @AuthenticationPrincipal UserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "워크스페이스 참여 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "워크스페이스 참여 예시",
                                    value = "{\"inviteCode\":\"AB12CD34\"}"
                            )
                    )
            )
            @Valid @RequestBody JoinWorkspaceRequest request
    ) {
        JoinWorkspaceResponse response = workspaceService.joinWorkspace(userDetails.getUsername(), request);
        return new ReturnMessage<>(response);
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    @Operation(summary = "워크스페이스 멤버 강퇴", description = "호스트 권한으로 대상 멤버를 워크스페이스에서 강퇴합니다.")
    public ReturnMessage<Void> removeMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId,
            @PathVariable Long userId
    ) {
        workspaceService.removeMember(userDetails.getUsername(), workspaceId, userId);
        return new ReturnMessage<>((Void) null);
    }

    @PatchMapping("/{workspaceId}/transfer-host")
    @Operation(summary = "워크스페이스 호스트 양도", description = "현재 호스트를 멤버로 변경하고 대상 멤버를 새 호스트로 양도합니다.")
    public ReturnMessage<TransferHostResponse> transferHost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "호스트 양도 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "호스트 양도 예시",
                                    value = "{\"targetUserId\":2}"
                            )
                    )
            )
            @Valid @RequestBody TransferHostRequest request
    ) {
        TransferHostResponse response = workspaceService.transferHost(userDetails.getUsername(), workspaceId, request);
        return new ReturnMessage<>(response);
    }

    @DeleteMapping("/{workspaceId}")
    @Operation(summary = "워크스페이스 삭제", description = "소유하고 있는 워크스페이스를 삭제합니다.")
    public ReturnMessage<Void> deleteWorkspace(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId
    ) {
        workspaceService.deleteWorkspace(userDetails.getUsername(), workspaceId);
        return new ReturnMessage<>(null);
    }
}

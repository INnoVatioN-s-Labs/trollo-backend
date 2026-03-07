package com.toyproject.trollo.controller;

import com.toyproject.trollo.common.util.ReturnMessage;
import com.toyproject.trollo.dto.workspace.CreateWorkspaceRequest;
import com.toyproject.trollo.dto.workspace.WorkspaceResponse;
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

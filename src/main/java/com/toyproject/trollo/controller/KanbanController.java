package com.toyproject.trollo.controller;

import com.toyproject.trollo.common.util.ReturnMessage;
import com.toyproject.trollo.dto.kanban.KanbanWorkspaceResponse;
import com.toyproject.trollo.service.KanbanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/kanban")
@RequiredArgsConstructor
@Tag(name = "Kanban", description = "칸반 통합 조회 API")
public class KanbanController {

    private final KanbanService kanbanService;

    @GetMapping
    @Operation(summary = "칸반 통합 조회", description = "워크스페이스의 보드/티켓을 위치 순서대로 통합 조회합니다.")
    public ReturnMessage<KanbanWorkspaceResponse> getKanban(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId
    ) {
        KanbanWorkspaceResponse response = kanbanService.getKanban(userDetails.getUsername(), workspaceId);
        return new ReturnMessage<>(response);
    }
}

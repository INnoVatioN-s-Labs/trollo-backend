package com.toyproject.trollo.controller;

import com.toyproject.trollo.common.util.ReturnMessage;
import com.toyproject.trollo.dto.board.BoardResponse;
import com.toyproject.trollo.dto.board.CreateBoardRequest;
import com.toyproject.trollo.dto.board.ReorderBoardRequest;
import com.toyproject.trollo.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/boards")
@RequiredArgsConstructor
@Tag(name = "Board", description = "보드 API")
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    @Operation(summary = "보드 생성", description = "워크스페이스의 마지막 위치 + 1로 보드를 생성합니다.")
    public ReturnMessage<BoardResponse> createBoard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "보드 생성 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "보드 생성 예시",
                                    value = "{\"name\":\"Todo\"}"
                            )
                    )
            )
            @Valid @RequestBody CreateBoardRequest request
    ) {
        BoardResponse response = boardService.createBoard(userDetails.getUsername(), workspaceId, request);
        return new ReturnMessage<>(response);
    }

    @PatchMapping("/{boardId}/reorder")
    @Operation(summary = "보드 순서 변경", description = "같은 워크스페이스 내에서 보드 위치를 변경합니다.")
    public ReturnMessage<BoardResponse> reorderBoard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId,
            @PathVariable Long boardId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "보드 순서 변경 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "순서 변경 예시",
                                    value = "{\"targetPosition\":1}"
                            )
                    )
            )
            @Valid @RequestBody ReorderBoardRequest request
    ) {
        BoardResponse response = boardService.reorderBoard(userDetails.getUsername(), workspaceId, boardId, request);
        return new ReturnMessage<>(response);
    }

    @DeleteMapping("/{boardId}")
    @Operation(summary = "보드 삭제", description = "보드를 삭제하고 워크스페이스 내 보드 위치를 정리합니다.")
    public ReturnMessage<Void> deleteBoard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId,
            @PathVariable Long boardId
    ) {
        boardService.deleteBoard(userDetails.getUsername(), workspaceId, boardId);
        return new ReturnMessage<>((Void) null);
    }
}

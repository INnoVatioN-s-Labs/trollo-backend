package com.toyproject.trollo.controller;

import com.toyproject.trollo.common.util.ReturnMessage;
import com.toyproject.trollo.dto.ticket.CreateTicketRequest;
import com.toyproject.trollo.dto.ticket.MoveTicketRequest;
import com.toyproject.trollo.dto.ticket.TicketResponse;
import com.toyproject.trollo.dto.ticket.UpdateTicketRequest;
import com.toyproject.trollo.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/boards/{boardId}/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket", description = "티켓 API")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @Operation(summary = "티켓 생성", description = "보드의 마지막 위치 + 1로 티켓을 생성합니다.")
    public ReturnMessage<TicketResponse> createTicket(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId,
            @PathVariable Long boardId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "티켓 생성 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "티켓 생성 예시",
                                    value = "{\"title\":\"API 스펙 정리\",\"description\":\"요청/응답 필드를 확정합니다.\"}"
                            )
                    )
            )
            @Valid @RequestBody CreateTicketRequest request
    ) {
        TicketResponse response = ticketService.createTicket(userDetails.getUsername(), workspaceId, boardId, request);
        return new ReturnMessage<>(response);
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "티켓 조회", description = "티켓 ID로 내용을 조회합니다.")
    public ReturnMessage<TicketResponse> getTicket(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId,
            @PathVariable Long boardId,
            @PathVariable Long ticketId
    ) {
        TicketResponse response = ticketService.getTicket(userDetails.getUsername(), workspaceId, boardId, ticketId);
        return new ReturnMessage<>(response);
    }

    @PatchMapping("/{ticketId}")
    @Operation(summary = "티켓 수정", description = "티켓 제목/설명을 수정합니다.")
    public ReturnMessage<TicketResponse> updateTicket(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId,
            @PathVariable Long boardId,
            @PathVariable Long ticketId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "티켓 수정 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "티켓 수정 예시",
                                    value = "{\"title\":\"API 스펙 정리 완료\",\"description\":\"확정된 요청/응답 필드를 반영합니다.\"}"
                            )
                    )
            )
            @Valid @RequestBody UpdateTicketRequest request
    ) {
        TicketResponse response = ticketService.updateTicket(
                userDetails.getUsername(), workspaceId, boardId, ticketId, request
        );
        return new ReturnMessage<>(response);
    }

    @PatchMapping("/{ticketId}/move")
    @Operation(summary = "티켓 이동", description = "같은 워크스페이스 내 보드/위치로 티켓을 이동합니다.")
    public ReturnMessage<TicketResponse> moveTicket(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId,
            @PathVariable Long boardId,
            @PathVariable Long ticketId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "티켓 이동 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "티켓 이동 예시",
                                    value = "{\"targetBoardId\":102,\"targetPosition\":1}"
                            )
                    )
            )
            @Valid @RequestBody MoveTicketRequest request
    ) {
        TicketResponse response = ticketService.moveTicket(
                userDetails.getUsername(), workspaceId, boardId, ticketId, request
        );
        return new ReturnMessage<>(response);
    }

    @DeleteMapping("/{ticketId}")
    @Operation(summary = "티켓 삭제", description = "티켓을 삭제하고 보드 내 위치를 정리합니다.")
    public ReturnMessage<Void> deleteTicket(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workspaceId,
            @PathVariable Long boardId,
            @PathVariable Long ticketId
    ) {
        ticketService.deleteTicket(userDetails.getUsername(), workspaceId, boardId, ticketId);
        return new ReturnMessage<>((Void) null);
    }
}

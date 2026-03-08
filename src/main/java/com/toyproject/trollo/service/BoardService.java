package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.board.BoardResponse;
import com.toyproject.trollo.dto.board.CreateBoardRequest;
import com.toyproject.trollo.dto.board.ReorderBoardRequest;
import com.toyproject.trollo.entity.ActivityType;
import com.toyproject.trollo.entity.Board;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
import com.toyproject.trollo.repository.BoardRepository;
import com.toyproject.trollo.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final TicketRepository ticketRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final ActivityLogService activityLogService;

    @Transactional
    public BoardResponse createBoard(String ownerEmail, Long workspaceId, CreateBoardRequest request) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        Workspace workspace = workspaceAccessService.getWorkspace(workspaceId);
        workspaceAccessService.getMembership(workspaceId, owner.getId());
        int nextPosition = boardRepository.findMaxPositionByWorkspaceId(workspaceId) + 1;

        Board savedBoard = boardRepository.save(Board.builder()
                .name(request.name())
                .position(nextPosition)
                .workspace(workspace)
                .build());
        activityLogService.saveLog(workspace, owner, ActivityType.BOARD_CREATE, "보드를 생성했습니다: " + savedBoard.getName());
        return toResponse(savedBoard);
    }

    @Transactional
    public BoardResponse reorderBoard(String ownerEmail, Long workspaceId, Long boardId, ReorderBoardRequest request) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        Workspace workspace = workspaceAccessService.getWorkspace(workspaceId);
        workspaceAccessService.getMembership(workspaceId, owner.getId());

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
        if (!board.getWorkspace().getId().equals(workspaceId)) {
            throw new BusinessException(ErrorCode.BOARD_WORKSPACE_MISMATCH);
        }

        int currentPosition = board.getPosition();
        int targetPosition = request.targetPosition();

        int maxPosition = boardRepository.findMaxPositionByWorkspaceId(workspaceId);
        if (targetPosition < 1 || targetPosition > maxPosition) {
            throw new BusinessException(ErrorCode.BOARD_INVALID_POSITION);
        }

        if (targetPosition == currentPosition) {
            return toResponse(board);
        }

        board.updatePosition(0);
        boardRepository.flush();

        if (targetPosition < currentPosition) {
            boardRepository.shiftRight(workspaceId, currentPosition, targetPosition);
        } else {
            boardRepository.shiftLeft(workspaceId, currentPosition, targetPosition);
        }

        board.updatePosition(targetPosition);
        Board reorderedBoard = boardRepository.save(board);
        activityLogService.saveLog(workspace, owner, ActivityType.BOARD_REORDER, "보드 순서를 변경했습니다: " + reorderedBoard.getName());

        return toResponse(reorderedBoard);
    }

    @Transactional
    public void deleteBoard(String ownerEmail, Long workspaceId, Long boardId) {
        User owner = workspaceAccessService.getUserByEmail(ownerEmail);
        Workspace workspace = workspaceAccessService.getWorkspace(workspaceId);
        workspaceAccessService.getMembership(workspaceId, owner.getId());

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
        if (!board.getWorkspace().getId().equals(workspaceId)) {
            throw new BusinessException(ErrorCode.BOARD_WORKSPACE_MISMATCH);
        }

        int currentPosition = board.getPosition();
        ticketRepository.deleteByBoardId(boardId);
        boardRepository.delete(board);
        boardRepository.flush();
        boardRepository.closeGap(workspaceId, currentPosition);
        activityLogService.saveLog(workspace, owner, ActivityType.BOARD_DELETE, "보드를 삭제했습니다: " + board.getName());
    }

    private BoardResponse toResponse(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getName(),
                board.getPosition(),
                board.getWorkspace().getId()
        );
    }

}

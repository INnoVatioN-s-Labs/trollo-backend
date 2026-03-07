package com.toyproject.trollo.service;

import com.toyproject.trollo.common.code.ErrorCode;
import com.toyproject.trollo.common.exception.BusinessException;
import com.toyproject.trollo.dto.board.BoardResponse;
import com.toyproject.trollo.dto.board.CreateBoardRequest;
import com.toyproject.trollo.dto.board.ReorderBoardRequest;
import com.toyproject.trollo.entity.Board;
import com.toyproject.trollo.entity.User;
import com.toyproject.trollo.entity.Workspace;
import com.toyproject.trollo.repository.BoardRepository;
import com.toyproject.trollo.repository.UserRepository;
import com.toyproject.trollo.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Transactional
    public BoardResponse createBoard(String ownerEmail, Long workspaceId, CreateBoardRequest request) {
        User owner = getUserByEmail(ownerEmail);
        Workspace workspace = getWorkspaceWithAccessCheck(workspaceId, owner.getId());
        int nextPosition = boardRepository.findMaxPositionByWorkspaceId(workspaceId) + 1;

        Board savedBoard = boardRepository.save(Board.builder()
                .name(request.name())
                .position(nextPosition)
                .workspace(workspace)
                .build());

        return toResponse(savedBoard);
    }

    @Transactional
    public BoardResponse reorderBoard(String ownerEmail, Long workspaceId, Long boardId, ReorderBoardRequest request) {
        User owner = getUserByEmail(ownerEmail);
        getWorkspaceWithAccessCheck(workspaceId, owner.getId());

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

        return toResponse(reorderedBoard);
    }

    private User getUserByEmail(String ownerEmail) {
        return userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Workspace getWorkspaceWithAccessCheck(Long workspaceId, Long ownerId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_NOT_FOUND));
        if (!workspace.getOwner().getId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
        return workspace;
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

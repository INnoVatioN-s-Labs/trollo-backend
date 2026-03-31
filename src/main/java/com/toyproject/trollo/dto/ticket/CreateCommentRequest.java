package com.toyproject.trollo.dto.ticket;

public record CreateCommentRequest(
    String content,
    Long parentId // nullable (for replies)
) {}

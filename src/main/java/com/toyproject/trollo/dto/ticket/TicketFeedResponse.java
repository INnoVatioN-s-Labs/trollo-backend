package com.toyproject.trollo.dto.ticket;

import com.toyproject.trollo.entity.ActivityType;
import java.time.LocalDateTime;

public record TicketFeedResponse(
    Long id,
    FeedType type,
    ActivityType activityType, // nullable depending on type
    String content,
    Long authorId,
    String authorName,
    LocalDateTime createdAt
) {}

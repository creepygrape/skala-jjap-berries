package com.jjap.berries.concert.dto;

import java.util.List;

public record SeatBulkCreateResponse(
    String section,
    int startNumber,
    int endNumber,
    int createdCount,
    List<SeatResponse> seats) {}

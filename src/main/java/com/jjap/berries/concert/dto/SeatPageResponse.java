package com.jjap.berries.concert.dto;

import java.util.List;

public record SeatPageResponse(
    List<SeatResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last) {
  public static SeatPageResponse of(List<SeatResponse> content, int page, int size, long total) {
    int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
    return new SeatPageResponse(
        content, page, size, total, totalPages, page == 0, totalPages == 0 || page >= totalPages - 1);
  }
}

package com.jjap.berries.global.common.response;

public record ApiErrorResponse(boolean success, String code, String message) {

    public static ApiErrorResponse failure(String code, String message) {
        return new ApiErrorResponse(false, code, message);
    }
}

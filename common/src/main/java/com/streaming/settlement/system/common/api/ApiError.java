package com.streaming.settlement.system.common.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.lang.model.type.ErrorType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiError {

    private ApiErrorType errorType;
    private String code;
    private String message;
    private String errorStack;

    public ApiError(ApiErrorType errorType, String code, String message) {
        this.errorType = errorType;
        this.code = code;
        this.message = message;
    }

    public ApiError(ApiErrorType errorType, String code, String message, Throwable e) {
        this.errorType = errorType;
        this.code = code;
        this.message = message;
        this.errorStack = getStackTraceAsString(e);
    }

    private String getStackTraceAsString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

}

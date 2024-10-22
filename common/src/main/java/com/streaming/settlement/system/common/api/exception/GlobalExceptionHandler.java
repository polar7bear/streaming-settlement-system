package com.streaming.settlement.system.common.api.exception;

import com.streaming.settlement.system.common.api.ApiError;
import com.streaming.settlement.system.common.api.ApiErrorType;
import com.streaming.settlement.system.common.api.ApiResponse;
import com.streaming.settlement.system.common.api.exception.member.DuplicateMemberException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = {DuplicateMemberException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateMemberException(DuplicateMemberException e) {
        return new ApiError(ApiErrorType.CONFLICT, "409", "이미 존재하는 회원입니다.", e);
    }
}

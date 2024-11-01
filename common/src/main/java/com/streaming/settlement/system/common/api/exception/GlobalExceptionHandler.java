package com.streaming.settlement.system.common.api.exception;

import com.streaming.settlement.system.common.api.ApiError;
import com.streaming.settlement.system.common.api.ApiErrorType;
import com.streaming.settlement.system.common.api.ApiResponse;
import com.streaming.settlement.system.common.api.exception.batch.BatchJobException;
import com.streaming.settlement.system.common.api.exception.member.DuplicateMemberException;
import com.streaming.settlement.system.common.api.exception.member.WrongPasswordException;
import com.streaming.settlement.system.common.api.exception.streaming.NotFoundStreamingException;
import com.streaming.settlement.system.common.api.exception.streaming.NotFoundStreamingViewLogException;
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

    @ResponseBody
    @ExceptionHandler(value = {WrongPasswordException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleWrongPasswordException(WrongPasswordException e) {
        return new ApiError(ApiErrorType.BAD_REQUEST, "400", "비밀번호가 일치하지 않습니다.", e);
    }

    @ResponseBody
    @ExceptionHandler(value = {NotFoundStreamingException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundStreamingException(NotFoundStreamingException e) {
        return new ApiError(ApiErrorType.NOT_FOUND, "404", "존재하지 않는 동영상 혹은 재생이 불가능한 동영상입니다.", e);
    }

    @ResponseBody
    @ExceptionHandler(value = {NotFoundStreamingViewLogException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundStreamingViewLogException(NotFoundStreamingViewLogException e) {
        return new ApiError(ApiErrorType.NOT_FOUND, "404", "시청 기록을 찾을 수 없습니다.", e);
    }

    @ResponseBody
    @ExceptionHandler(value = {BatchJobException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError BatchJobException(BatchJobException e) {
        return new ApiError(ApiErrorType.NOT_FOUND, "404", "찾을 수 없는 Job 입니다.", e);
    }

}

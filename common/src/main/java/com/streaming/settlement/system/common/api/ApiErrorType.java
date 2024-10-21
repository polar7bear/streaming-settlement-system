package com.streaming.settlement.system.common.api;

import lombok.Getter;

@Getter
public enum ApiErrorType {
    AUTHENTICATION(403),
    BAD_REQUEST(400),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    NETWORK(502),
    SERVER_ERROR(500),
    UNKNOWN(500)
    ;

    int httpStatusCode;

    ApiErrorType(int httpStatusCode){
        this.httpStatusCode = httpStatusCode;
    }
}

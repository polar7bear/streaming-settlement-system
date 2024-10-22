package com.streaming.settlement.system.common.api;

import lombok.Getter;

@Getter
public enum ApiErrorType {
    // https://developer.mozilla.org/ko/docs/Web/HTTP/Status#%ED%81%B4%EB%9D%BC%EC%9D%B4%EC%96%B8%ED%8A%B8_%EC%97%90%EB%9F%AC_%EC%9D%91%EB%8B%B5
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    REQUEST_TIMEOUT(408),
    CONFLICT(409),

    INTERNAL_SERVER_ERROR(500),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504);

    final int httpStatusCode;

    ApiErrorType(int httpStatusCode){
        this.httpStatusCode = httpStatusCode;
    }
}

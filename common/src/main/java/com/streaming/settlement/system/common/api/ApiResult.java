package com.streaming.settlement.system.common.api;

public class ApiResult<T> {
    private boolean success = true;

    private ApiError error;

    private T data;

    private long responseTime = -1;

    private boolean checkedContext(){
        return ResponseContext.requestAt.get() != null && ResponseContext.requestAt.get() != null;
    }

    public ApiResult() {
        if(checkedContext()){
            this.responseTime = System.currentTimeMillis() - ResponseContext.requestAt.get();
        }
    }

    public ApiResult(boolean success, ApiError error) {
        this.success = success;
        this.error = error;
        if(checkedContext()){
            this.responseTime = System.currentTimeMillis() - ResponseContext.requestAt.get();
        }
    }

    public ApiResult(T data) {
        this.data = data;
        if(checkedContext()){
            this.responseTime = System.currentTimeMillis() - ResponseContext.requestAt.get();
        }
    }

    public ApiResult(Throwable e, String errorCode, String message){
        this.error = new ApiError(ApiErrorType.UNKNOWN, errorCode, message, e);
        this.success = false;

        if(checkedContext()){
            this.responseTime = System.currentTimeMillis() - ResponseContext.requestAt.get();
        }
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", error=" + error +
                ", data=" + data +
                ", responseTime=" + responseTime +
                '}';
    }
}

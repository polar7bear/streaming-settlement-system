package com.streaming.settlement.system.streamingadservice.controller;

import com.streaming.settlement.system.common.api.ApiResponse;
import com.streaming.settlement.system.streamingadservice.service.StreamingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/streams")
@RequiredArgsConstructor
public class StreamingController {

    private final StreamingService streamingService;

    @PostMapping("/play/{streamingId}")
    public ApiResponse<Integer> playStreaming(
            @PathVariable Long streamingId,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId,
            @RequestParam(required = false) Integer currentTime,
            HttpServletRequest request) {

        Integer startTime = streamingService.playStreaming(streamingId, memberId, currentTime == null ? 0 : currentTime, request.getHeader("X-Forwarded-For"));
        return new ApiResponse<>("동영상 재생 성공하였습니다.", startTime);
    }

    @PostMapping("/pause/{streamingId}")
    public ApiResponse<Void> pauseStreaming(
            @PathVariable Long streamingId,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId,
            @RequestParam Integer currentTime,
            HttpServletRequest request) {
        streamingService.pauseStreaming(streamingId, memberId, request.getRemoteAddr(), currentTime);
        return new ApiResponse<>("동영상이 중지되었습니다.");
    }
}

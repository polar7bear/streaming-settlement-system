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
    public ApiResponse<String> playStreaming(
            @PathVariable Long streamingId,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId,
            @RequestParam(required = false) Integer currentTime,
            HttpServletRequest request) {

        streamingService.playStreaming(streamingId, memberId, currentTime, request.getRemoteAddr());
        return new ApiResponse<>("동영상 재생 성공하였습니다.");
    }

    @PostMapping("/pause/{streamingId}")
    public ApiResponse<Void> pauseStreaming(
            @PathVariable Long streamingId,
            @RequestParam Long memeberId,
            @RequestParam Integer currentTime) {

        return null;
    }
}

package com.streaming.settlement.system.memberservice.interfaces.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {


    @GetMapping("/")
    public String hi() {
        return "hi";
    }
}

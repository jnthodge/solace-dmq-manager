package com.example.dmqmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {"/", "/{path:[^\\.]*}", "/**/{path:[^\\.]*}"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}

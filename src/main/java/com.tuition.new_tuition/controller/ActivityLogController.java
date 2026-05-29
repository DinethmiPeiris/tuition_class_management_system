package com.tuition.new_tuition.controller;

import com.tuition.new_tuition.service.ActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/e5/activity")
public class ActivityLogController {

    private final ActivityLogService service;

    public ActivityLogController(ActivityLogService service) {
        this.service = service;
    }

    @GetMapping("/logs")
    public String logs(Model model) {
        model.addAttribute("logs", service.listAll());
        return "e5/activity/logs";
    }
}

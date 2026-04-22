package com.smarttracker.controller;

import com.smarttracker.dto.ProgressResponse;
import com.smarttracker.model.User;
import com.smarttracker.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public ResponseEntity<ProgressResponse> getProgress(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(progressService.getProgress(user.getId()));
    }
}

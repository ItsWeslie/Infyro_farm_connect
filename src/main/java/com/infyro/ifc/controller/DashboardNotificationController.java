package com.infyro.ifc.controller;

import com.infyro.ifc.dto.notification.DashboardNotificationResponse;
import com.infyro.ifc.service.DashboardNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class DashboardNotificationController {
    private final DashboardNotificationService service;

    @GetMapping
    public List<DashboardNotificationResponse> list(Principal principal,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(principal.getName(), unreadOnly, page, size);
    }

    @GetMapping("/unread-count")
    public long unreadCount(Principal principal) {
        return service.unreadCount(principal.getName());
    }

    @PatchMapping("/{id}/read")
    public DashboardNotificationResponse markRead(@PathVariable Long id, Principal principal) {
        return service.markRead(id, principal.getName());
    }
}

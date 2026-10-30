package com.example.gestionalquilerback.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class CapabilitiesController {
    private final String commit;
    public CapabilitiesController(@Value("${RENDER_GIT_COMMIT:unknown}") String commit) { this.commit = commit; }

    @GetMapping("/api/capabilities")
    public Capabilities capabilities() {
        return new Capabilities(2, List.of("safe-writes", "soft-archive", "investment", "recurrences", "leases", "documents", "filtered-reports"), commit);
    }

    public record Capabilities(int apiVersion, List<String> features, String commit) {}
}

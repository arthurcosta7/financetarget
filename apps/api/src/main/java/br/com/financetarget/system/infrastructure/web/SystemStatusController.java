package br.com.financetarget.system.infrastructure.web;

import br.com.financetarget.system.application.SystemStatusService;
import br.com.financetarget.system.domain.SystemStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemStatusController {

    private final SystemStatusService statusService;

    public SystemStatusController(SystemStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    public ResponseEntity<SystemStatus> status() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(statusService.currentStatus());
    }
}

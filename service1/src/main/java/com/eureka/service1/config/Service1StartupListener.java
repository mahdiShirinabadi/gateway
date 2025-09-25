package com.eureka.service1.config;

import com.eureka.service1.service.AclRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Service1 Startup Listener
 * Registers Service1 methods with ACL service on startup
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class Service1StartupListener {

    private final AclRegistrationService aclRegistrationService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Service1 application started - registering with ACL service");
        
        try {
            boolean result = aclRegistrationService.registerService1Methods();
            if (result) {
                log.info("Service1 successfully registered with ACL service");
            } else {
                log.warn("Failed to register Service1 with ACL service");
            }
        } catch (Exception e) {
            log.error("Error registering Service1 with ACL service: {}", e.getMessage());
        }
    }
}

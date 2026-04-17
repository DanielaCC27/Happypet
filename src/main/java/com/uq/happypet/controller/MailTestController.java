package com.uq.happypet.controller;

import com.uq.happypet.service.EmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/mail")
@ConditionalOnProperty(name = "app.mail.test-endpoint-enabled", havingValue = "true")
public class MailTestController {

    private final EmailService emailService;

    public MailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-verification")
    public ResponseEntity<String> sendVerificationTest(@RequestParam String to) {
        String token = "test-token-" + System.currentTimeMillis();
        boolean ok = emailService.sendVerificationEmail(to, token);
        if (!ok) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Send failed. Check server console and SMTP settings.");
        }
        return ResponseEntity.ok("Test email sent to " + to + ". Check inbox and spam.");
    }
}
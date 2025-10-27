package com.email.writer;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/email")
public class EmailGeneratorController {

    private final EmailGeneratorService emailService;
    private final Bucket bucket;

    public EmailGeneratorController(EmailGeneratorService emailService) {
        this.emailService = emailService;
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder().addLimit(limit).build();
    }

    @PostMapping(value = "/generate", produces = "application/json")

    public ResponseEntity<Object> generate(@RequestBody EmailRequest request) {

        if (bucket.tryConsume(1)) {

            String replyText = emailService.generateEmailReply(request);
            return ResponseEntity.ok(new EmailResponse(replyText)); // Return 200 OK
        }

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS) // Return 429
                .body("You have made too many requests. Please try again in a minute.");
    }
}

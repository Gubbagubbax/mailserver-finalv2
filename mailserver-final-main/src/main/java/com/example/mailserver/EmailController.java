package com.example.mailserver;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/emails")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<?> createEmail(@RequestBody @Valid Email email) {
        try {
            Email created = emailService.createEmail(email);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Feil ved oppretting av e-post: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllEmails() {
        try {
            List<Email> emails = emailService.getAllEmails();
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Feil ved henting av e-poster: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmail(@PathVariable Long id) {
        try {
            emailService.deleteEmail(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Feil ved sletting av e-post: " + e.getMessage());
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody @Valid EmailRequest request) {
        try {
            emailService.sendEmail(
                request.getFromEmail(),
                request.getToEmail(),
                request.getSubject(),
                request.getBody()
            );
            return ResponseEntity.ok("E-post sendt!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Feil ved sending av e-post: " + e.getMessage());
        }
    }
}

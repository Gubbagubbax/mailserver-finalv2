package com.example.mailserver;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emails")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<?> createEmail(@RequestBody Email email) {
        try {
            if (email.getFromEmail() == null ||
                    email.getToEmail() == null ||
                    email.getSubject() == null ||
                    email.getBody() == null) {

                logger.warn("Oppretting av e-post feilet: Minst ett felt er null: from={}, to={}, subject={}, body={}",
                        email.getFromEmail(), email.getToEmail(), email.getSubject(), email.getBody());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Alle feltene (fromEmail, toEmail, subject, body) må være utfylt.");
            }

            Email created = emailService.createEmail(email);
            logger.info("E-post opprettet med ID {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Feil ved oppretting av e-post", e);
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
            logger.error("Feil ved henting av e-poster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Feil ved henting av e-poster: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmail(@PathVariable Long id) {
        try {
            emailService.deleteEmail(id);
            logger.info("E-post med ID {} er slettet", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Feil ved sletting av e-post med ID " + id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Feil ved sletting av e-post: " + e.getMessage());
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody EmailRequest request) {
        try {
            if (request.getFromEmail() == null ||
                    request.getToEmail() == null ||
                    request.getSubject() == null ||
                    request.getBody() == null) {

                logger.warn("Sending av e-post feilet: Minst ett felt er null: from={}, to={}, subject={}, body={}",
                        request.getFromEmail(), request.getToEmail(), request.getSubject(), request.getBody());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Alle feltene (fromEmail, toEmail, subject, body) må være utfylt.");
            }

            emailService.sendEmail(
                    request.getFromEmail(),
                    request.getToEmail(),
                    request.getSubject(),
                    request.getBody());
            logger.info("E-post sendt fra {} til {}", request.getFromEmail(), request.getToEmail());
            return ResponseEntity.ok("E-post sendt!");
        } catch (Exception e) {
            logger.error("Feil ved sending av e-post", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Feil ved sending av e-post: " + e.getMessage());
        }
    }

    // 🔧 NYTT: PUT /emails/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmail(@PathVariable Long id, @RequestBody Email updatedEmail) {
        try {
            if (updatedEmail.getFromEmail() == null ||
                    updatedEmail.getToEmail() == null ||
                    updatedEmail.getSubject() == null ||
                    updatedEmail.getBody() == null) {

                logger.warn("Oppdatering feilet: Minst ett felt er null: from={}, to={}, subject={}, body={}",
                        updatedEmail.getFromEmail(), updatedEmail.getToEmail(),
                        updatedEmail.getSubject(), updatedEmail.getBody());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Alle feltene (fromEmail, toEmail, subject, body) må være utfylt for oppdatering.");
            }

            List<Email> emails = emailService.getAllEmails();
            for (Email existingEmail : emails) {
                if (existingEmail.getId().equals(id)) {
                    existingEmail.setFromEmail(updatedEmail.getFromEmail());
                    existingEmail.setToEmail(updatedEmail.getToEmail());
                    existingEmail.setSubject(updatedEmail.getSubject());
                    existingEmail.setBody(updatedEmail.getBody());
                    existingEmail.setTimestamp(updatedEmail.getTimestamp());

                    Email saved = emailService.createEmail(existingEmail);
                    logger.info("E-post med ID {} oppdatert", id);
                    return ResponseEntity.ok(saved);
                }
            }

            logger.warn("Ingen e-post funnet med ID {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("E-post med ID " + id + " ble ikke funnet.");
        } catch (Exception e) {
            logger.error("Feil ved oppdatering av e-post med ID " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Feil ved oppdatering: " + e.getMessage());
        }
    }
}

package com.example.mailserver;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailRepository emailRepository;

    public Email createEmail(Email email) {
        if (email == null) {
            logger.warn("Mottok null e-post-objekt – ignorerer.");
            throw new IllegalArgumentException("E-post kan ikke være null");
        }

        logger.info("Oppretter ny e-post fra {} til {}", 
            email.getFromEmail(), 
            email.getToEmail()
        );

        if (email.getTimestamp() == null) {
            email.setTimestamp(LocalDateTime.now());
            logger.info("Setter automatisk timestamp til {}", email.getTimestamp());
        }

        return emailRepository.save(email);
    }

    public List<Email> getAllEmails() {
        logger.info("Henter alle e-poster fra databasen");
        return emailRepository.findAll();
    }

    public void deleteEmail(Long id) {
        logger.info("Sletter e-post med ID {}", id);
        emailRepository.deleteById(id);
    }

    public void sendEmail(String fromEmail, String toEmail, String subject, String body) {
        logger.info("Prøver å sende e-post fra {} til {} med emne: {}", fromEmail, toEmail, subject);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("E-post sendt!");
        } catch (IllegalArgumentException | org.springframework.mail.MailException e) {
            logger.error("E-postfeil: {}", e.getMessage());
            throw new RuntimeException("Kunne ikke sende e-post", e);
        }
    }
}

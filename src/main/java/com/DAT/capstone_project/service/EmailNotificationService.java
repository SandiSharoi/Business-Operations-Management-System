package com.DAT.capstone_project.service;

import com.DAT.capstone_project.event.FormStatusChangeEvent;
import com.DAT.capstone_project.model.FormApplyEntity;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {
    private final EmailSender emailSender;

    @EventListener
    public void onFormStatusChange(FormStatusChangeEvent event) {
        FormApplyEntity formApply = event.getFormApply();
        sendEmailWithRetry(formApply, 3);  // Retry up to 3 times if email fails
    }

    private void sendEmailWithRetry(FormApplyEntity formApply, int maxRetries) {
        String recipientEmail = formApply.getEmployee().getEmail();
        String subject = "Your Form Has Been " + formApply.getFinalFormStatus();
        String content = String.format(
            "<p>Dear %s,</p><p>Your form submitted on %s has been <b>%s</b>.</p>",
            formApply.getEmployee().getName(), formApply.getAppliedDate(), formApply.getFinalFormStatus()
        );

        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                emailSender.sendEmail(recipientEmail, subject, content);
                log.info("✅ Email sent successfully to {}", recipientEmail);
                return; // Exit loop if successful
            } catch (MessagingException | UnsupportedEncodingException e) {
                attempt++;
                log.error("❌ Failed to send email (Attempt {}/{}): {}", attempt, maxRetries, e.getMessage());
                try {
                    Thread.sleep(2000); // Wait 2 seconds before retrying
                } catch (InterruptedException ignored) {}
            }
        }
        log.error("🚨 Email sending failed after {} attempts for {}", maxRetries, recipientEmail);
    }
}

package com.texastoc.module.notification;

import com.texastoc.module.notification.service.EmailService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NotificationModuleImpl implements NotificationModule {

  private final EmailService emailService;

  public NotificationModuleImpl(EmailService emailService) {
    this.emailService = emailService;
  }

  @Override
  public void sendEmail(List<String> emails, String subject, String body) {
    emailService.send(emails, subject, body);
  }

  @Override
  public void sendText(String phone, String message) {

  }
}

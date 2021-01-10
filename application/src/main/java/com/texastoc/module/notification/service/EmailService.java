package com.texastoc.module.notification.service;

import com.texastoc.module.notification.connector.EmailConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmailService {

  private final EmailConnector emailConnector;

  public EmailService(EmailConnector emailConnector) {
    this.emailConnector = emailConnector;
  }

  // TODO send email method
  public void send(List<String> emails, String subject, String body) {

  }
}

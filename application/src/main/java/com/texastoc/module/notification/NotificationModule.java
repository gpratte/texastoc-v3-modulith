package com.texastoc.module.notification;

import java.util.List;

public interface NotificationModule {

  /**
   * Send an email
   * @param emails
   * @param subject
   * @param body
   */
  public void sendEmail(List<String> emails, String subject, String body);

}

package com.texastoc.connector;

import com.texastoc.repository.PlayerRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class SMSConnector {

  private ExecutorService executorService;
  private boolean initialzed;

  private final String twilioPhone;
  private final PlayerRepository playerRepository;

  public SMSConnector(@Value("${twilio.sid:#{null}}") String sid,
                      @Value("${twilio.token:#{null}}") String token,
                      @Value("${twilio.phone:#{null}}") String phone,
                      PlayerRepository playerRepository) {
    twilioPhone = "+1" + phone;
    this.playerRepository = playerRepository;
    try {
      Twilio.init(sid, token);
      initialzed = true;
      executorService = Executors.newFixedThreadPool(20);
    } catch (Exception e) {
      log.error("Could not initialize Twilio", e);
      initialzed = false;
    }
  }

  public void text(String phone, String body) {
    if (!initialzed) return;
    executorService.submit(new SendSMS(phone, body));
  }

  class SendSMS implements Callable<Void> {
    private String phone;
    private String body;

    public SendSMS(String phone, String body) {
      this.phone = phone;
      this.body = body;
    }

    @Override
    public Void call() throws Exception {
      try {
        Message.creator(new PhoneNumber("+1" + phone), // to
          new PhoneNumber(twilioPhone), // from
          body).create();
      } catch (Exception e) {
        log.error("Could not send SMS", e);
      }
      return null;
    }
  }
}

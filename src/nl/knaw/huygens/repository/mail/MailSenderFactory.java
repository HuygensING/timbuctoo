package nl.knaw.huygens.repository.mail;

import nl.knaw.huygens.repository.config.Configuration;

import com.google.inject.Inject;

public class MailSenderFactory {
  private boolean enabled;
  private String host;
  private String port;
  private String fromAddress;

  @Inject
  public MailSenderFactory(Configuration config) {
    enabled = config.getBooleanSetting("mail.enabled");
    host = config.getSetting("mail.host");
    port = config.getSetting("mail.port");
    fromAddress = config.getSetting("mail.fromAddress");
  }

  public MailSender create() {
    if (enabled) {
      return new MailSenderImpl(host, port, fromAddress);
    }

    return new MailSender() {

      @Override
      public void sendMail(String recipient, String subject, String content) {
        // TODO Auto-generated method stub

      }
    };
  }
}

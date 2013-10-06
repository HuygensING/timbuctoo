package nl.knaw.huygens.timbuctoo.mail;

import com.google.inject.Inject;

public class MailSenderFactory {

  private boolean enabled;
  private String host;
  private String port;
  private String fromAddress;

  @Inject
  public MailSenderFactory(boolean isMailEnabled, String host, String portNumber, String fromAddress) {
    enabled = isMailEnabled;
    this.host = host;
    port = portNumber;
    this.fromAddress = fromAddress;
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

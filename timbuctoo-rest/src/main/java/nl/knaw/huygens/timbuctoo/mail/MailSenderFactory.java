package nl.knaw.huygens.timbuctoo.mail;

import com.google.inject.Inject;

public class MailSenderFactory {

  private final boolean enabled;
  private final String host;
  private final String port;
  private final String fromAddress;

  @Inject
  public MailSenderFactory(boolean isMailEnabled, String host, String port, String fromAddress) {
    enabled = isMailEnabled;
    this.host = host;
    this.port = port;
    this.fromAddress = fromAddress;
  }

  public MailSender create() {
    if (enabled) {
      return new MailSenderImpl(host, port, fromAddress);
    } else {
      return new NoMailSender();
    }
  }

}

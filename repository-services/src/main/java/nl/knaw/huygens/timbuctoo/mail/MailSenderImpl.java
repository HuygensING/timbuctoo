package nl.knaw.huygens.timbuctoo.mail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class MailSenderImpl implements MailSender {

  private static final String CONTENT_CHAR_SET = "utf-8";

  private static final String TRANSPORT_TYPE = "smtp";

  private static final String POST_PROPERTY = "mail.smtp.port";

  private static final String HOST_PROPERTY = "mail.smtp.host";

  private static final Logger LOG = LoggerFactory.getLogger(MailSenderImpl.class);

  private String fromAddress;
  private Session session;

  @Inject
  public MailSenderImpl(@Named("mail.host")
  String host, @Named("mail.port")
  String port, @Named("mail.from_address")
  String fromAddress) {
    this.fromAddress = fromAddress;

    Properties emailProperties = new Properties();
    emailProperties.setProperty(HOST_PROPERTY, host);
    emailProperties.setProperty(POST_PROPERTY, port);

    session = Session.getInstance(emailProperties);

  }

  /* (non-Javadoc)
   * @see nl.knaw.huygens.timbuctoo.mail.MailSender#sendMail(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void sendMail(String recipients, String subject, String content) {

    MimeMessage message = createMessage(recipients, subject, content);

    Transport transport;
    try {
      transport = session.getTransport(TRANSPORT_TYPE);
      transport.connect();
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
    } catch (NoSuchProviderException e) {
      e.printStackTrace();
      LOG.error(e.getMessage());
    } catch (MessagingException e) {
      e.printStackTrace();
      LOG.error(e.getMessage());
    }

  }

  private MimeMessage createMessage(String recipients, String subject, String content) {
    MimeMessage message = new MimeMessage(session);
    try {
      message.setFrom(new InternetAddress(fromAddress));
      message.setRecipients(RecipientType.TO, recipients);
      message.setSubject(subject);
      message.setText(content, CONTENT_CHAR_SET);
      message.setSentDate(new Date());
    } catch (AddressException e) {
      e.printStackTrace();
      LOG.error(e.getMessage());
    } catch (MessagingException e) {
      e.printStackTrace();
      LOG.error(e.getMessage());
    }
    return message;
  }

  public static void main(String[] args) {
    MailSender mailer = new MailSenderImpl("spinner.knaw.nl", "25", "noreply@huygens.knaw.nl");
    mailer.sendMail("martijn.maas@huygens.knaw.nl", "test", "dit is een test");
  }
}

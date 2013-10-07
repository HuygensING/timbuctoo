package nl.knaw.huygens.timbuctoo.mail;

public class NoMailSender implements MailSender {

  @Override
  public void sendMail(String recipient, String subject, String content) {}

}

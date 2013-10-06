package nl.knaw.huygens.timbuctoo.mail;

public interface MailSender {

  /**
   * Send an email to one or more recipients.
   * @param recipient a string with comma-separated email-addresses. 
   * @param subject
   * @param content
   */
  public abstract void sendMail(String recipient, String subject, String content);

}

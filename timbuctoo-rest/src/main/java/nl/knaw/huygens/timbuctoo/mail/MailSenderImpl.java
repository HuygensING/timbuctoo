package nl.knaw.huygens.timbuctoo.mail;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

  @Override
  public void sendMail(String recipients, String subject, String content) {
    MimeMessage message = createMessage(recipients, subject, content);
    try {
      Transport transport = session.getTransport(TRANSPORT_TYPE);
      transport.connect();
      transport.sendMessage(message, message.getAllRecipients());
      transport.close();
    } catch (NoSuchProviderException e) {
      LOG.error(e.getMessage());
    } catch (MessagingException e) {
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
      LOG.error(e.getMessage());
    } catch (MessagingException e) {
      LOG.error(e.getMessage());
    }
    return message;
  }

  public static void main(String[] args) {
    MailSender mailer = new MailSenderImpl("spinner.knaw.nl", "25", "noreply@huygens.knaw.nl");
    mailer.sendMail("martijn.maas@huygens.knaw.nl", "test", "dit is een test");
  }

}

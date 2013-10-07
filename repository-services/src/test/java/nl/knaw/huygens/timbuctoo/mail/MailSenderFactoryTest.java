package nl.knaw.huygens.timbuctoo.mail;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MailSenderFactoryTest {

  @Test
  public void testDisabledMailSender() {
    MailSenderFactory factory = new MailSenderFactory(false, "spinner.knaw.nl", "25", "noreply@huygens.knaw.nl");
    assertTrue(factory.create() instanceof NoMailSender);
  }

}

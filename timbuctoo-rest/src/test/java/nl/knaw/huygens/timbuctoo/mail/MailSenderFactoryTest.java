package nl.knaw.huygens.timbuctoo.mail;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MailSenderFactoryTest {

  @Test
  public void testDisabledMailSender() {
    MailSenderFactory factory = new MailSenderFactory(false, "spinner.knaw.nl", "25", "noreply@huygens.knaw.nl");
    assertTrue(factory.create() instanceof NoMailSender);
  }

}

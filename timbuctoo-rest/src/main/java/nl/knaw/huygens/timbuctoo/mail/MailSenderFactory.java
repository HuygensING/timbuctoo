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

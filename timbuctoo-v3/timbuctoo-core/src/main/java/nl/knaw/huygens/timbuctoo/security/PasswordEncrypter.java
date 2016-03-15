package nl.knaw.huygens.timbuctoo.security;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class PasswordEncrypter {

  private MessageDigest messageDigest;

  public PasswordEncrypter() throws NoSuchAlgorithmException {
    messageDigest = MessageDigest.getInstance("SHA-256");
  }

  public String encryptPassword(String password, String salt) {
    return encryptPassword(password, salt.getBytes());
  }

  public String encryptPassword(String password, byte[] salt) {
    messageDigest.reset();
    messageDigest.update(salt);
    byte[] encryptedAuth = messageDigest.digest(password.getBytes());
    return new String(encryptedAuth);
  }

  public byte[] createSalt() {
    return UUID.randomUUID().toString().getBytes();
  }

}

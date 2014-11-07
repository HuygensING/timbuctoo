package nl.knaw.huygens.timbuctoo.security;

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

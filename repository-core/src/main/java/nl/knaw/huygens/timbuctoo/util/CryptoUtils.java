package nl.knaw.huygens.timbuctoo.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import nl.knaw.huygens.timbuctoo.model.User;

import org.apache.commons.codec.binary.Base64;
import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

public class CryptoUtils {

  public static boolean checkHash(String plain, String result) {
    return BCrypt.checkpw(plain, result);
  }

  public static String generatePwHash(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  private static Date cookieHashKeyExpirationDate = null;
  private static SecretKey cookieHashKey = null;
  private static SecretKey lastCookieHashKey = null;
  private static ObjectMapper objectMapper;
  /**
   * The time for which a given cookie key stays valid, in milliseconds.
   * Currently set to 5 days.
   */
  private static final long COOKIE_EXPIRATION_PERIOD = 5 * 24 * 60 * 60 * 1000;
  private static KeyGenerator keyGen;
  static {
    try {
      keyGen = KeyGenerator.getInstance("HmacSHA512");
    } catch (NoSuchAlgorithmException e) {
      keyGen = null;
      e.printStackTrace();
    }
  }

  private static SecretKey getCookieHashKey() {
    if (cookieHashKey == null || (cookieHashKeyExpirationDate != null && cookieHashKeyExpirationDate.before(new Date()))) {
      if (cookieHashKey != null) {
        lastCookieHashKey = cookieHashKey;
      }
      cookieHashKey = keyGen.generateKey();
      cookieHashKeyExpirationDate = new Date(System.currentTimeMillis() + COOKIE_EXPIRATION_PERIOD);
    }
    return cookieHashKey;
  }

  public static String createCookie(String userInfo) throws Exception {
    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    SecretKey key = getCookieHashKey();
    return getCookieString(userInfo, date, key);
  }

  private static String getCookieString(String userInfo, String date, SecretKey key) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA512");
    mac.init(key);
    byte[] macResult = mac.doFinal(getPlainStringForCookie(userInfo, date));
    return date + ";;;" + (new Base64()).encodeToString(macResult);
  }

  private static byte[] getPlainStringForCookie(String userInfo, String date) throws UnsupportedEncodingException {
    return (userInfo + ";;;" + date).getBytes("UTF-8");
  }

  public static boolean verifyCookie(String userInfo, String cookie) {
    // First, separate the date and the cookie:
    String[] dateAndHash = cookie.split(";;;");
    if (dateAndHash.length != 2) {
      return false;
    }
    String date = dateAndHash[0];

    // Then check the date is within the valid range:
    try {
      Date d = new SimpleDateFormat("yyyy-MM-dd").parse(date);
      if (d.before(new Date(System.currentTimeMillis() - COOKIE_EXPIRATION_PERIOD))) {
        return false;
      }
    } catch (ParseException e) {
      return false;
    }
    try {
      // Then check that the userinfo, date and the most recent key match:
      if (getCookieString(userInfo, date, getCookieHashKey()).equals(cookie)) {
        return true;
      }
      // Otherwise, see if we have an old key:
      if (lastCookieHashKey == null) {
        return false;
      }
      // If we do, check that too:
      if (getCookieString(userInfo, date, lastCookieHashKey).equals(cookie)) {
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public static User getUserFromInfo(String userInfo) {
    try {
      return getObjectMapper().readValue(userInfo, User.class);
    } catch (IOException e) {
      return null;
    }
  }

  private static ObjectMapper getObjectMapper() {
    if (objectMapper == null) {
      objectMapper = new ObjectMapper();
    }
    return objectMapper;
  }

  public static String getInfoFromUser(User user) {
    Map<String, Object> userInfo = Maps.newHashMap();
    userInfo.put("firstName", user.displayName);
    userInfo.put("_id", user.getId());
    userInfo.put("groups", user.groups);
    try {
      return getObjectMapper().writeValueAsString(userInfo);
    } catch (IOException ex) {
      return "";
    }
  }
}

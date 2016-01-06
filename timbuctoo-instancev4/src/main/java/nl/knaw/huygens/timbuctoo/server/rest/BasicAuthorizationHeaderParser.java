package nl.knaw.huygens.timbuctoo.server.rest;

import java.util.Base64;

public class BasicAuthorizationHeaderParser {

  //FIXME: IllegalArgumentException when the string does not start with 'Basic '

  /**
   * Parses the string Basic [some base64 hash] into a username and password
   *
   * @throws IllegalArgumentException When the header does not start with 'Basic ', when the hash is not valid Base64
   *                                  encoded, or when there is no : in the decoded string.
   */
  public static Credentials authenticate(String authorizationHeader) {


    if (!authorizationHeader.toLowerCase().startsWith("basic ")) {
      throw new IllegalArgumentException("Header must start with the word 'Basic' followed by 1 space.");
    }
    int indexOfWhitespace = authorizationHeader.indexOf(" ");
    String rawAuthString = authorizationHeader.substring(indexOfWhitespace + 1);

    String decodedAuth = new String(Base64.getDecoder().decode(rawAuthString.getBytes()));
    if (!decodedAuth.contains(":")) {
      throw new IllegalArgumentException(String
        .format("The username and password should be seperated by a ':' but that character was not found in '%s'.",
          decodedAuth));
    }

    int indexOfFirstColon = decodedAuth.indexOf(":");
    String username = decodedAuth.substring(0, indexOfFirstColon);
    String password = decodedAuth.substring(indexOfFirstColon + 1);

    return new Credentials(username, password);
  }

  public static class Credentials {
    private final String username;
    private final String password;

    public Credentials(String username, String password) {

      this.username = username;
      this.password = password;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }
  }
}

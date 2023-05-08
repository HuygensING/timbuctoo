package nl.knaw.huygens.timbuctoo.server.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;


public class BasicAuthorizationHeaderParserTest {

  public static final String KNOWN_USER = "knownUser";
  public static final String CORRECT_PASSWORD = "correctPassword";
  public static final String VALID_AUTH_STRING = String.format("%s:%s", KNOWN_USER, CORRECT_PASSWORD);


  @Test
  public void parseReturnsATokenWhenTheAuthenticationStringIsValid() throws InvalidAuthorizationHeaderException {
    //A valid header is Basic YTpi
    //'YTpi' decodes to a:b

    BasicAuthorizationHeaderParser.Credentials result = BasicAuthorizationHeaderParser.parse("Basic YTpi");
    assertThat(result.getUsername(), is("a"));
    assertThat(result.getPassword(), is("b"));
  }


  @Test
  public void parseSupportsColonsInPasswords() throws InvalidAuthorizationHeaderException {
    BasicAuthorizationHeaderParser.Credentials result = null;
    try {
      result = BasicAuthorizationHeaderParser.parse(makeHeader("user:test:password"));
    } finally {
      assertThat(result.getPassword(), is("test:password"));
    }
  }

  @Test
  public void parseSupportsBasicIsSpelledWithLowercaseB() throws InvalidAuthorizationHeaderException {
    String encodedAuthString = encodeBase64(VALID_AUTH_STRING);
    String header = String.format("basic %s", encodedAuthString);

    BasicAuthorizationHeaderParser.Credentials result = BasicAuthorizationHeaderParser.parse(header);

    assertThat(result, is(not(nullValue()))); //the real assertion is that no error is thrown
  }

  @Test
  public void parseSupportsBasicIsSpelledInAllUppercase() throws InvalidAuthorizationHeaderException {
    String encodedAuthString = encodeBase64(VALID_AUTH_STRING);
    String header = String.format("BASIC %s", encodedAuthString);

    BasicAuthorizationHeaderParser.Credentials result = BasicAuthorizationHeaderParser.parse(header);

    assertThat(result, is(not(nullValue())));
  }

  @Test
  public void parseRequiresTheHeaderToStartWithBasic() {
    String encodedAuthString = encodeBase64(VALID_AUTH_STRING);
    String header = String.format("absic %s", encodedAuthString);

    assertThrows(InvalidAuthorizationHeaderException.class, () -> BasicAuthorizationHeaderParser.parse(header));
  }


  @Test
  public void parseFailsWhenTheHeaderOnlyHasTheHash() {
    String encodedAuthString = encodeBase64(VALID_AUTH_STRING);
    String header = String.format("%s", encodedAuthString);

    assertThrows(InvalidAuthorizationHeaderException.class, () -> BasicAuthorizationHeaderParser.parse(header));
  }

  @Test
  public void parseThrowsAnInvalidAuthorizationHeaderExceptionIfTheAuthenticationStringIsInvalidBase64() {
    assertThrows(InvalidAuthorizationHeaderException.class, () ->
        BasicAuthorizationHeaderParser.parse("Basic Unencoded%AuthString"));
  }

  @Test
  public void parseThrowsAnInvalidAuthorizationHeaderExceptionIfTheAuthenticationDoesNotContainAColon() {
    assertThrows(InvalidAuthorizationHeaderException.class, () ->
        BasicAuthorizationHeaderParser.parse(makeHeader("InvalidAuthString")));
  }
  
  private String makeHeader(String authString) {
    String encodedAuthString = encodeBase64(authString);
    return String.format("Basic %s", encodedAuthString);
  }

  private String encodeBase64(String valid) {
    return new String(Base64.getEncoder().encode(valid.getBytes()));
  }
}

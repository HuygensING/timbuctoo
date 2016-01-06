package nl.knaw.huygens.timbuctoo.server.rest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Base64;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class BasicAuthorizationHeaderParserTest {

  public static final String KNOWN_USER = "knownUser";
  public static final String CORRECT_PASSWORD = "correctPassword";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  public static final String VALID_AUTH_STRING = String.format("%s:%s", KNOWN_USER, CORRECT_PASSWORD);

  @Test
  public void authenticateThrowsAnIllegalArgumentExceptionIfTheAuthenticationStringIsInvalid() {

    expectedException.expect(IllegalArgumentException.class);

    BasicAuthorizationHeaderParser.authenticate(makeHeader("InvalidAuthString"));
  }

  @Test
  public void authenticateReturnsATokenWhenTheAuthenticationStringIsValid() {
    //A valid header is Basic YTpi
    //'YTpi' decodes to a:b

    BasicAuthorizationHeaderParser.Credentials result = BasicAuthorizationHeaderParser.authenticate("Basic YTpi");
    assertThat(result.getUsername(), is("a"));
    assertThat(result.getPassword(), is("b"));
  }


  @Test
  public void authenticateSupportsColonsInPasswords() {
    BasicAuthorizationHeaderParser.Credentials result = null;
    try {
      result = BasicAuthorizationHeaderParser.authenticate(makeHeader("user:test:password"));
    } finally {
      assertThat(result.getPassword(), is("test:password"));
    }
  }

  @Test
  public void authenticateSupportsBasicIsSpelledWithLowercaseB() {
    String encodedAuthString = encodeBase64(VALID_AUTH_STRING);
    String header = String.format("basic %s", encodedAuthString);

    BasicAuthorizationHeaderParser.Credentials result = BasicAuthorizationHeaderParser.authenticate(header);

    assertThat(result, is(not(nullValue())));//the real assertion is that no error is thrown
  }

  @Test
  public void authenticateSupportsBasicIsSpelledInAllUppercase() {
    String encodedAuthString = encodeBase64(VALID_AUTH_STRING);
    String header = String.format("BASIC %s", encodedAuthString);

    BasicAuthorizationHeaderParser.Credentials result = BasicAuthorizationHeaderParser.authenticate(header);

    assertThat(result, is(not(nullValue())));
  }

  @Test
  public void authenticateRequiresTheHeaderToStartWithBasic() {
    String encodedAuthString = encodeBase64(VALID_AUTH_STRING);
    String header = String.format("absic %s", encodedAuthString);

    expectedException.expect(IllegalArgumentException.class);

    BasicAuthorizationHeaderParser.authenticate(header);
  }

  @Test
  public void authenticateFailsWhenTheHeaderOnlyHasTheHash() {
    String encodedAuthString = encodeBase64(VALID_AUTH_STRING);
    String header = String.format("%s", encodedAuthString);

    expectedException.expect(IllegalArgumentException.class);

    BasicAuthorizationHeaderParser.authenticate(header);
  }


  private String makeHeader(String authString) {
    String encodedAuthString = encodeBase64(authString);
    return String.format("Basic %s", encodedAuthString);
  }

  private String encodeBase64(String valid) {
    return new String(Base64.getEncoder().encode(valid.getBytes()));
  }
}

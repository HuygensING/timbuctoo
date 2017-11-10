package nl.knaw.huygens.timbuctoo.security;

import org.junit.Before;
import org.junit.Test;

public class TwitterOAuthTest {
  private TwitterOAuth twitterOAuth;

  @Before
  public void setUp() throws Exception {
    twitterOAuth = new TwitterOAuth();
  }

  //@Test
  public void testTwitterOAuth() throws Exception {
    twitterOAuth.twitterAuth();
  }
}

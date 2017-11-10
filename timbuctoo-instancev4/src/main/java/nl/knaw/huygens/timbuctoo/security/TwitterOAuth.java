package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.v5.graphql.serializable.SerializerExecutionStrategy;
import org.slf4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.slf4j.LoggerFactory.getLogger;

public class TwitterOAuth {
  private static final Logger LOG = getLogger(TwitterOAuth.class);
  private String consumerKey = "bzY4pW3bdU3UbcDmsotJE5S54";
  private String consumerSecret = "9Dp2IfbDPpS8mMhB8CA9eBgOQP1fGTLguE0oAzrY98m02kBkmp";

  public TwitterOAuth() {
  }

  public void twitterAuth() throws TwitterException, IOException {
    Twitter twitter = TwitterFactory.getSingleton();
    twitter.setOAuthConsumer(consumerKey, consumerSecret);
    RequestToken requestToken = twitter.getOAuthRequestToken();
    AccessToken accessToken = null;
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    while (null == accessToken) {
      System.out.println("Open the following URL and grant access to your account:");
      System.out.println(requestToken.getAuthorizationURL());

      System.out.println("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
      String pin = br.readLine();

      try {
        accessToken = twitter.getOAuthAccessToken();
      } catch (TwitterException te) {
        if (401 == te.getStatusCode()) {
          System.out.println("Unable to get the access token.");
        } else {
          te.printStackTrace();
        }
      }
    }
  }
}

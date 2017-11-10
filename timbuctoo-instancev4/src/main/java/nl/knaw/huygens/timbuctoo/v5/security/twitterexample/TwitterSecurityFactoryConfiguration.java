package nl.knaw.huygens.timbuctoo.v5.security.twitterexample;

import nl.knaw.huygens.timbuctoo.server.security.OldStyleSecurityFactoryConfiguration;
import nl.knaw.huygens.timbuctoo.v5.security.SecurityFactory;
import org.apache.http.impl.client.CloseableHttpClient;

public class TwitterSecurityFactoryConfiguration extends OldStyleSecurityFactoryConfiguration {
  @Override
  public SecurityFactory createNewSecurityFactory(CloseableHttpClient httpCaller) {
    return new TwitterSecurityFactory(super.createNewSecurityFactory(httpCaller));
  }
}

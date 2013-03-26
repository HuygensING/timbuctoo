package nl.knaw.huygens.repository.resources;

import java.net.URI;
import java.util.Set;

import com.google.inject.Injector;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.WebApplicationFactory;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.impl.container.inmemory.TestResourceClientHandler;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;

// Heavily inspired by http://randomizedsort.blogspot.nl/2012/07/jersey-unit-testing-with-guice-and.html
public class GuiceTestContainerFactory implements TestContainerFactory {

  private final Injector injector;

  public GuiceTestContainerFactory(Injector injector) {
    this.injector = injector;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<LowLevelAppDescriptor> supports() {
    return LowLevelAppDescriptor.class;
  }

  @Override
  public TestContainer create(URI baseUri, AppDescriptor ad) throws IllegalArgumentException {

    return new GuiceTestContainer(baseUri, (LowLevelAppDescriptor) ad, injector);
  }

  private static final class GuiceTestContainer implements TestContainer {
    private final URI baseUri;
    private final WebApplication webApplication;
    private final Injector injector;
    private final ResourceConfig resourceConfig;

    public GuiceTestContainer(URI baseUri, LowLevelAppDescriptor appDescriptor, Injector injector) {
      this.baseUri = baseUri;
      resourceConfig = appDescriptor.getResourceConfig();
      this.webApplication = WebApplicationFactory.createWebApplication();
      this.injector = injector;
    }

    @Override
    // copied from InMemoryTestContainerFactory.InMemoryTestContainer
    public Client getClient() {
      ClientConfig clientConfig = null;
      Set<Object> providerSingletons = resourceConfig.getProviderSingletons();

      if (providerSingletons.size() > 0) {
        clientConfig = new DefaultClientConfig();
        for (Object providerSingleton : providerSingletons) {
          clientConfig.getSingletons().add(providerSingleton);
        }
      }

      Client client = (clientConfig == null) ? new Client(new TestResourceClientHandler(baseUri, webApplication)) : new Client(new TestResourceClientHandler(baseUri, webApplication), clientConfig);

      return client;
    }

    @Override
    public URI getBaseUri() {
      return baseUri;
    }

    @Override
    public void start() {
      if (!webApplication.isInitiated()) {
        webApplication.initiate(resourceConfig, new GuiceComponentProviderFactory(resourceConfig, injector));
      }
    }

    @Override
    public void stop() {
      if (webApplication.isInitiated()) {
        webApplication.destroy();
      }

    }

  }

}

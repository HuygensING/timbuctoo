package nl.knaw.huygens.repository.rest;

import java.io.File;
import java.util.EnumSet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.ResourceCollection;

import com.google.inject.servlet.GuiceFilter;

/**
 * 
 * 
 *
 */
// inspired by: https://hajix.wordpress.com/2014/08/07/starting-a-simple-server-with-jettyjerseyguicejackson-stack/
public class EmbeddedServer {

  private static final String PATH = "/timbuctoo";
  private static final int PORT_NUMBER = 8080;

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      throw new RuntimeException("Add a relative path to config.js as first argument for example: ../timbuctoo-war-config-example/src/main/webapp/");
    }

    new EmbeddedServer().start(args[0]);
  }

  public void start(String relativePathToJsConfig) throws Exception, InterruptedException {
    Server server = new Server(PORT_NUMBER);

    setupContext(server, relativePathToJsConfig);

    server.start();
    server.join();
  }

  public void setupContext(Server server, String relativePathToJSConfig) {
    ServletContextHandler context = new ServletContextHandler(server, PATH, ServletContextHandler.SESSIONS);
    context.addFilter(GuiceFilter.class, "/*", EnumSet.<javax.servlet.DispatcherType> of(javax.servlet.DispatcherType.REQUEST, javax.servlet.DispatcherType.ASYNC));
    context.addEventListener(new RepoContextListener());
    context.addServlet(DefaultServlet.class, "/*");

    ResourceHandler resourceHandler = createResourceHandler(relativePathToJSConfig);

    context.setHandler(resourceHandler);
  }

  private ResourceHandler createResourceHandler(String relativePathToJSConfig) {
    ResourceCollection resourceCollection = new ResourceCollection(new String[] { getHtmlLocation(), getJSConfigLocation(relativePathToJSConfig) });

    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setBaseResource(resourceCollection);
    resourceHandler.setDirectoriesListed(true);
    resourceHandler.setWelcomeFiles(new String[] { "static/index.html" });
    return resourceHandler;
  }

  private String getJSConfigLocation(String relativePathToJSConfig) {
    File file = new File(relativePathToJSConfig);
    return file.getAbsolutePath();
  }

  public String getHtmlLocation() {
    File file = new File("../timbuctoo-webpages/src/main/webapp/");
    return file.getAbsolutePath();
  }
}

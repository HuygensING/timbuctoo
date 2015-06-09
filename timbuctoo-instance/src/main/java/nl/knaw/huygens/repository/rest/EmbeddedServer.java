package nl.knaw.huygens.repository.rest;

import java.io.File;
import java.util.EnumSet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.google.inject.servlet.GuiceFilter;

// inspired by: https://hajix.wordpress.com/2014/08/07/starting-a-simple-server-with-jettyjerseyguicejackson-stack/
public class EmbeddedServer {

  private static final String PATH = "/timbuctoo";
  private static final int PORT_NUMBER = 8080;

  public static void main(String[] args) throws Exception {
    new EmbeddedServer().start();
  }

  public void start() throws Exception, InterruptedException {
    Server server = new Server(PORT_NUMBER);

    setupContext(server);

    server.start();
    server.join();
  }

  // static content inspired by: http://stackoverflow.com/questions/9385432/how-do-i-set-up-static-resources-and-custom-services-with-embedded-jetty
  public void setupContext(Server server) {
    ServletContextHandler context = new ServletContextHandler(server, PATH, ServletContextHandler.SESSIONS);
    context.addFilter(GuiceFilter.class, "/*", EnumSet.<javax.servlet.DispatcherType> of(javax.servlet.DispatcherType.REQUEST, javax.servlet.DispatcherType.ASYNC));
    context.addEventListener(new RepoContextListener());
    context.addServlet(DefaultServlet.class, "/*");

    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setResourceBase(getHtmlLocation());
    resourceHandler.setDirectoriesListed(true);
    resourceHandler.setWelcomeFiles(new String[] { "static/index.html" });

    context.setHandler(resourceHandler);

    String htmlLoc = getHtmlLocation();

    System.out.println("html location: " + htmlLoc);
  }

  public String getHtmlLocation() {
    File file = new File("../timbuctoo-webpages/src/main/webapp/");
    return file.getAbsolutePath();
  }
}

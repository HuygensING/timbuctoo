package nl.knaw.huygens.repository.importer;

import java.io.IOException;

import nl.knaw.huygens.repository.BasicInjectionModule;

import org.apache.commons.configuration.ConfigurationException;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SetupDatabase {
  public static void main(String[] args) throws ConfigurationException, IOException {
    Injector injector = Guice.createInjector(new BasicInjectionModule("config.xml"));
    // FIXME: this should be configurable, and for that we need a commandline parsing tool.
    String vreId = "test-vre";
    String vreName = "Test VRE";

    DatabaseSetupper setupper = injector.getInstance(DatabaseSetupper.class);
    setupper.setVREId(vreId, vreName);
    System.exit(setupper.run());
  }

}

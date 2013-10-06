package nl.knaw.huygens.timbuctoo.tools.importer;

import nl.knaw.huygens.timbuctoo.config.BasicInjectionModule;
import nl.knaw.huygens.timbuctoo.config.Configuration;

import com.google.inject.Guice;
import com.google.inject.Injector;

@Deprecated
public class SetupDatabase {

  public static void main(String[] args) throws Exception {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new BasicInjectionModule(config));
    // FIXME: this should be configurable, and for that we need a commandline parsing tool.
    String vreId = "test-vre";
    String vreName = "Test VRE";

    DatabaseSetupper setupper = injector.getInstance(DatabaseSetupper.class);
    setupper.setVREId(vreId, vreName);
    System.exit(setupper.run());
  }

}

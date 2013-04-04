package nl.knaw.huygens.repository.importer;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import nl.knaw.huygens.repository.RepositoryBasicModule;

public class SetupDatabase {
	public static void main(String[] args) throws ConfigurationException, IOException {
	  Injector injector = Guice.createInjector(new RepositoryBasicModule("config.xml"));
	  // FIXME: this should be configurable, and for that we need a commandline parsing tool.
	  String vreId = "test-vre";
	  String vreName = "Test VRE";
	  
	  DatabaseSetupper setupper = injector.getInstance(DatabaseSetupper.class);
	  setupper.setVREId(vreId, vreName);
	  System.exit(setupper.run());
	}
}

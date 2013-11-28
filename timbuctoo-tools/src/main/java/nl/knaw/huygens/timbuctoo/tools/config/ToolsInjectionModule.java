package nl.knaw.huygens.timbuctoo.tools.config;

import nl.knaw.huygens.timbuctoo.config.BasicInjectionModule;
import nl.knaw.huygens.timbuctoo.config.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to make it possible to use Guice @see http://code.google.com/p/google-guice . 
 */
public class ToolsInjectionModule extends BasicInjectionModule {

  private static final Logger LOG = LoggerFactory.getLogger(ToolsInjectionModule.class);

  public ToolsInjectionModule(Configuration config) {
    super(config);
    LOG.info("In constructor");
  }

}

package nl.knaw.huygens.timbuctoo.tools.config;

import nl.knaw.huygens.timbuctoo.config.BasicInjectionModule;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.tools.messages.ToolsBroker;

public class ToolsInjectionModule extends BasicInjectionModule {

  public ToolsInjectionModule(Configuration config) {
    super(config);
  }

  @Override
  protected void configure() {
    bind(Broker.class).to(ToolsBroker.class);
    super.configure();
  }

}

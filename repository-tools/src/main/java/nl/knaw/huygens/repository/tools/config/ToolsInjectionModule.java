package nl.knaw.huygens.repository.tools.config;

import nl.knaw.huygens.repository.config.BasicInjectionModule;
import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.messages.Broker;
import nl.knaw.huygens.repository.tools.messages.ToolsBroker;

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

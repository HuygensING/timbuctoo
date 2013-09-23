package nl.knaw.huygens.repository.tools.config;

import nl.knaw.huygens.repository.config.BasicInjectionModule;
import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.messages.ActiveMQBroker;
import nl.knaw.huygens.repository.messages.Broker;

public class ToolsInjectionModule extends BasicInjectionModule{

  public ToolsInjectionModule(Configuration config) {
    super(config);
  }

  @Override
  protected void configure() {
    bind(Broker.class).to(ActiveMQBroker.class);
    super.configure();
  }
}

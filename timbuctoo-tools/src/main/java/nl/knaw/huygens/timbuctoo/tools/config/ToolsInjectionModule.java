package nl.knaw.huygens.timbuctoo.tools.config;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.configuration.ConfigurationException;

import nl.knaw.huygens.timbuctoo.XRepository;
import nl.knaw.huygens.timbuctoo.config.BasicInjectionModule;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.OldIndexManager;

/**
 * A class to make it possible to use Guice @see http://code.google.com/p/google-guice . 
 */
public class ToolsInjectionModule extends BasicInjectionModule {

  /**
   * Creates a configured repository instance.
   */
  public static XRepository createRepositoryInstance() throws ConfigurationException {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));
    return injector.getInstance(XRepository.class);
  }

  public ToolsInjectionModule(Configuration config) {
    super(config);
  }

  @Override
  protected void configure() {
    super.configure();
    bind(IndexManager.class).to(OldIndexManager.class);
  }

}

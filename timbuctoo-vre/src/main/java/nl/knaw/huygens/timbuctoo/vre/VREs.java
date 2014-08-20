package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo vre
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

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Provides access to the active VRE's from the configuration,
 * specified as a list of class names in the current package.
 */
@Singleton
public class VREs implements VRECollection {

  private static final Logger LOG = LoggerFactory.getLogger(VREs.class);

  private static final String VRE_CONFIG_KEY = "vres.vre";
  private static final String PACKAGE_NAME = "nl.knaw.huygens.timbuctoo.vre";

  private final List<VRE> vres = Lists.newArrayList();

  @Inject
  public VREs(Configuration config, IndexFactory indexFactory) {
    for (String name : config.getSettingList(VRE_CONFIG_KEY)) {
      try {
        String className = PACKAGE_NAME + "." + name;
        LOG.info("Adding {}", className);
        Class<?> cls = Class.forName(className);
        if (VRE.class.isAssignableFrom(cls)) {
          VRE vre = (VRE) cls.newInstance();
          vre.initIndexes(indexFactory);
          vres.add(vre);
        } else {
          LOG.error("Invalid VRE name {}", className);
          throw new RuntimeException("Invalid VRE");
        }
      } catch (Exception e) {
        LOG.error("Failed to initialize VRE: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        throw new RuntimeException("Invalid VRE");
      }
    }
  }

  @Override
  public List<VRE> getVREs() {
    return vres;
  }

}

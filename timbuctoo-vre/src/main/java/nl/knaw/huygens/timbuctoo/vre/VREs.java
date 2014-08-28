package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo VRE
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
import nl.knaw.huygens.timbuctoo.config.Configuration.VREDef;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Provides access to the configured VRE's.
 */
@Singleton
public class VREs implements VRECollection {

  private static final Logger LOG = LoggerFactory.getLogger(VREs.class);

  private final List<VRE> vres = Lists.newArrayList();

  @Inject
  public VREs(Configuration config, IndexFactory indexFactory) {
    for (VREDef vreDef : config.getVREDefs()) {
      LOG.info("Adding {} - {}", vreDef.id, vreDef.description);
      VRE vre = new PackageVRE(vreDef.id, vreDef.description, vreDef.modelPackage, vreDef.receptions);
      vre.initIndexes(indexFactory);
      vres.add(vre);
    }
  }

  @Override
  public List<VRE> getVREs() {
    return vres;
  }

}

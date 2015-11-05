package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo VRE
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.Configuration.VREDef;
import nl.knaw.huygens.timbuctoo.index.IndexFactory;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Provides access to the configured VRE's.
 */
@Singleton
public class VREs implements VRECollection {
  private static final Logger LOG = LoggerFactory.getLogger(VREs.class);

  private final Map<String, VRE> vres = Maps.newHashMap();

  @Inject
  public VREs(Configuration config, IndexFactory indexFactory, Repository repository, RelationSearcher relationSearcher) {
    for (VREDef vreDef : config.getVREDefs()) {
      LOG.info("Adding {} - {}", vreDef.id, vreDef.description);
      VRE vre = createVRE(repository, relationSearcher, vreDef);
      vre.initIndexes(indexFactory);
      String vreId = vreDef.id;
      vres.put(vreId, vre);
    }
  }

  private PackageVRE createVRE(Repository repository, RelationSearcher relationSearcher, VREDef vreDef) {
    if(isWomenWritersVRE(vreDef)){
      return new WomenWritersVRE(vreDef.id, vreDef.description, vreDef.modelPackage, vreDef.receptions, repository, relationSearcher);
    }

    return new PackageVRE(vreDef.id, vreDef.description, vreDef.modelPackage, repository, relationSearcher);
  }

  private boolean isWomenWritersVRE(VREDef vreDef) {
    return "WomenWriters".equals(vreDef.id);
  }

  @Override
  public List<VRE> getAll() {
    return Lists.newArrayList(vres.values());
  }

  @Override
  public boolean doesVREExist(String vreId) {
    return vres.containsKey(vreId);
  }

  @Override
  public VRE getVREById(String vreId) {
    return vres.get(vreId);
  }

}

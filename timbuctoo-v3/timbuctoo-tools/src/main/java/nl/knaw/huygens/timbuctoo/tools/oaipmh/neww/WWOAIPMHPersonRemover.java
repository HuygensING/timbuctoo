package nl.knaw.huygens.timbuctoo.tools.oaipmh.neww;

/*
 * #%L
 * Timbuctoo tools
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

import static nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule.createInjectorWithoutSolr;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.neww.WWPerson;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.tools.oaipmh.CMDIOAIIdentifierGenerator;
import nl.knaw.huygens.timbuctoo.tools.oaipmh.OaiPmhRestClient;
import nl.knaw.huygens.timbuctoo.tools.oaipmh.SetSpecGenerator;
import nl.knaw.huygens.timbuctoo.tools.oaipmh.VREIdUtils;

import org.apache.commons.configuration.ConfigurationException;

import com.google.common.base.Objects;
import com.google.inject.Injector;

public class WWOAIPMHPersonRemover {
  private static final String VRE_ID = "WomenWriters";

  public static void main(String[] args) throws ConfigurationException {
    Injector injector = createInjectorWithoutSolr();

    OaiPmhRestClient oaiPmhRestClient = injector.getInstance(OaiPmhRestClient.class);
    CMDIOAIIdentifierGenerator idGenerator = injector.getInstance(CMDIOAIIdentifierGenerator.class);
    Repository repository = injector.getInstance(Repository.class);
    SetSpecGenerator setSpecGenerator = injector.getInstance(SetSpecGenerator.class);

    try {
      removePersonSets(oaiPmhRestClient, setSpecGenerator);
      removeRecords(oaiPmhRestClient, idGenerator, repository);
    } finally {
      repository.close();
    }

  }

  private static void removePersonSets(OaiPmhRestClient oaiPmhRestClient, SetSpecGenerator setSpecGenerator) {
    for (String setSpec : setSpecGenerator.generate(new WWPerson(), VRE_ID)) {
      if (!Objects.equal(setSpec, VREIdUtils.simplifyVREId(VRE_ID))) {
        oaiPmhRestClient.deleteSet(setSpec);
      }
    }
  }

  private static void removeRecords(OaiPmhRestClient oaiPmhRestClient, CMDIOAIIdentifierGenerator idGenerator, Repository repository) {
    for (StorageIterator<WWPerson> iterator = repository.getDomainEntities(WWPerson.class); iterator.hasNext();) {
      WWPerson entity = iterator.next();
      String oaiId = idGenerator.generate(entity, VRE_ID);

      oaiPmhRestClient.deleteRecord(oaiId);
    }
  }
}

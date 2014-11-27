package nl.knaw.huygens.timbuctoo.tools.oaipmh.neww;

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

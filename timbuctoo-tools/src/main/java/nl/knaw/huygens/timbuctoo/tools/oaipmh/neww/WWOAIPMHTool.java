package nl.knaw.huygens.timbuctoo.tools.oaipmh.neww;

import static nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule.createInjectorWithoutSolr;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.neww.WWDocument;
import nl.knaw.huygens.timbuctoo.model.neww.WWPerson;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.tools.oaipmh.OAIRecordCreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

public class WWOAIPMHTool {
  private final static Logger LOG = LoggerFactory.getLogger(WWOAIPMHTool.class);
  private static String oaiUrl;

  public static void main(String args[]) throws Exception {
    String frontEndURL = "http://www.example.com";
    String vreId = "WomenWriters";
    Injector injector = createInjectorWithoutSolr();

    Configuration config = injector.getInstance(Configuration.class);
    oaiUrl = config.getSetting("oai-url");

    OAIRecordCreator oaiRecordCreator = injector.getInstance(OAIRecordCreator.class);
    Repository repo = injector.getInstance(Repository.class);

    try {
      createOAIRecordForThreeEntities(WWDocument.class, frontEndURL, vreId, oaiRecordCreator, repo);
      //createOAIRecordForAllEntities(WWDocument.class, frontEndURL, vreId, oaiRecordCreator, repo);
      createOAIRecordForThreeEntities(WWPerson.class, frontEndURL, vreId, oaiRecordCreator, repo);
      //createOAIRecordForAllEntities(WWPerson.class, frontEndURL, vreId, oaiRecordCreator, repo);
    } finally {
      repo.close();
    }
  }

  private static <T extends DomainEntity> void createOAIRecordForThreeEntities(Class<T> type, String frontEndURL, String vreId, OAIRecordCreator oaiRecordCreator, Repository repo) {
    for (T entity : repo.getDomainEntities(type).getSome(3)) {
      createOAIRecord(frontEndURL, vreId, oaiRecordCreator, entity);
    }

  }

  private static <T extends DomainEntity> void createOAIRecordForAllEntities(Class<T> type, String frontEndURL, String vreId, OAIRecordCreator oaiRecordCreator, Repository repo) {
    for (StorageIterator<T> iterator = repo.getDomainEntities(type); iterator.hasNext();) {
      T domainEntity = iterator.next();

      createOAIRecord(frontEndURL, vreId, oaiRecordCreator, domainEntity);
    }
  }

  private static void createOAIRecord(String frontEndURL, String vreId, OAIRecordCreator oaiRecordCreator, DomainEntity entity) {
    LOG.info("create meta data for \"{}\" with id \"{}\"", entity.getClass(), entity.getId());
    oaiRecordCreator.create(entity, vreId, frontEndURL, oaiUrl);
  }
}

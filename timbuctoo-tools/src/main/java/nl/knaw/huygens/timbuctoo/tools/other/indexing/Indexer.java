package nl.knaw.huygens.timbuctoo.tools.other.indexing;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import java.util.concurrent.CountDownLatch;

class Indexer implements Runnable {

  private final Repository repository;
  private final IndexManager indexManager;
  private final Class<? extends DomainEntity> type;
  private final CountDownLatch countDownLatch;

  public Indexer(Class<? extends DomainEntity> type, Repository repository, IndexManager indexManager,
                 CountDownLatch countDownLatch) {
    this.type = type;
    this.repository = repository;
    this.indexManager = indexManager;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run() {
    String typeName = TypeNames.getInternalName(type);
    VREReIndexer.LOG.info("Start indexing for {}.", typeName);
    StorageIterator<? extends DomainEntity> iterator = repository.getDomainEntities(type);
    while (iterator.hasNext()) {

      String id = iterator.next().getId();
      try {
        indexManager.addEntity(type, id);
      } catch (IndexException e) {
        VREReIndexer.LOG.error("Error indexing for {} with id {}.", typeName, id);
        VREReIndexer.LOG.debug("Error: {}", e);
      } catch (RuntimeException e) {
        VREReIndexer.LOG.error("Error indexing for {} with id {}.", typeName, id);
        VREReIndexer.LOG.debug("Error: {}", e);
        countDownLatch.countDown();
        throw e;
      }
    }

    iterator.close();

    VREReIndexer.LOG.info("End indexing for {}.", typeName);
    countDownLatch.countDown();
    VREReIndexer.LOG.info("Incomplete tasks: {}", countDownLatch.getCount());
  }
}

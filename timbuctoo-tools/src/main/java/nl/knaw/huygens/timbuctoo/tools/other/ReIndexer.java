package nl.knaw.huygens.timbuctoo.tools.other;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.process.Progress;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ReIndexer {
  private static final int MILLI_SECONDS_TO_MINUTES = 60000;
  public final static Logger LOG = LoggerFactory.getLogger(ReIndexer.class);

  public static void main(String[] args) throws ConfigurationException, IndexException, InterruptedException {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

    TypeRegistry registry = injector.getInstance(TypeRegistry.class);
    Repository repository = injector.getInstance(Repository.class);
    IndexManager indexManager = injector.getInstance(IndexManager.class);

    LOG.info("Clearing index");
    indexManager.deleteAllEntities();

    try {
      //      indexSynchronous(repository, indexManager, registry);
      indexAsynchrounous(repository, indexManager, registry);
    } finally {
      repository.close();
      indexManager.close();
      stopWatch.stop();
      LOG.info("Time used: {} m", (stopWatch.getTime() / (double) MILLI_SECONDS_TO_MINUTES));
    }
  }

  private static void indexAsynchrounous(Repository repository, IndexManager indexManager, TypeRegistry registry) throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(registry.getPrimitiveDomainEntityTypes().size());
    for (Class<? extends DomainEntity> primitiveType : registry.getPrimitiveDomainEntityTypes()) {
      Runnable indexer = new Indexer(primitiveType, repository, indexManager);
      executor.execute(indexer);
    }
    executor.shutdown();
    LOG.info("Indexing all done before closing? {}", executor.awaitTermination(20, TimeUnit.MINUTES));
  }

  @SuppressWarnings("unused")
  private static void indexSynchronous(Repository repository, IndexManager indexManager, TypeRegistry registry) throws IndexException {
    for (Class<? extends DomainEntity> primitiveType : registry.getPrimitiveDomainEntityTypes()) {
      Progress progress = new Progress();
      for (StorageIterator<? extends DomainEntity> iterator = repository.getDomainEntities(primitiveType); iterator.hasNext();) {
        indexManager.addEntity(primitiveType, iterator.next().getId());
        progress.step();
      }
      progress.done();
    }
  }

  private static class Indexer implements Runnable {

    private final Repository repository;
    private final IndexManager indexManager;
    private final Class<? extends DomainEntity> type;

    public Indexer(Class<? extends DomainEntity> type, Repository repository, IndexManager indexManager) {
      this.type = type;
      this.repository = repository;
      this.indexManager = indexManager;

    }

    @Override
    public void run() {
      String typeName = TypeNames.getInternalName(type);
      LOG.info("Start indexing for {}.", typeName);
      for (StorageIterator<? extends DomainEntity> iterator = repository.getDomainEntities(type); iterator.hasNext();) {
        try {
          indexManager.addEntity(type, iterator.next().getId());
        } catch (IndexException e) {
          LOG.error("Error indexing for {}.", typeName);
          LOG.debug("Error: {}", e);
        }
      }
      LOG.info("End indexing for {}.", typeName);
    }
  }

}

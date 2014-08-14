package nl.knaw.huygens.timbuctoo.tools.other;

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
  private static final int MILI_SECONDS_TO_MINUTES = 60000;
  public final static Logger LOG = LoggerFactory.getLogger(ReIndexer.class);

  public static void main(String[] args) throws ConfigurationException, IndexException {
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
      for (Class<? extends DomainEntity> primitiveType : registry.getPrimitiveDomainEntityTypes()) {
        LOG.info("indexing for: {}", TypeNames.getInternalName(primitiveType));
        Progress progress = new Progress();
        for (StorageIterator<? extends DomainEntity> iterator = repository.getDomainEntities(primitiveType); iterator.hasNext();) {
          indexManager.addEntity(primitiveType, iterator.next().getId());
          progress.step();
        }
        progress.done();
      }
    } finally {
      repository.close();
      indexManager.close();
      stopWatch.stop();
      LOG.info("Time used: {} m", (stopWatch.getTime() / (double) MILI_SECONDS_TO_MINUTES));
    }
  }

}

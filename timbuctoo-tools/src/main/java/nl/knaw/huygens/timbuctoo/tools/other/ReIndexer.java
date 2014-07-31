package nl.knaw.huygens.timbuctoo.tools.other;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.process.Progress;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ReIndexer {
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

    for (Class<? extends DomainEntity> primitiveType : registry.getPrimitiveDomainEntityTypes()) {
      LOG.info("indexing for: {}", TypeNames.getInternalName(primitiveType));
      Progress progress = new Progress();
      for (DomainEntity entity : repository.getDomainEntities(primitiveType).getAll()) {
        indexManager.addEntity(primitiveType, entity.getId());
        progress.step();
      }
      progress.done();
    }
    repository.close();
    indexManager.close();
    stopWatch.stop();
    System.out.println("time used: " + (stopWatch.getTime() / 1000));
  }
}

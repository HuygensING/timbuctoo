package nl.knaw.huygens.timbuctoo.tools.other.indexing;

import com.google.common.base.Stopwatch;
import com.google.inject.Injector;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREs;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.toList;

public class VREReIndexer {

  public static final Logger LOG = LoggerFactory.getLogger(VREReIndexer.class);

  public static void main(String[] args) throws ConfigurationException, InterruptedException, IndexException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    Injector injector = ToolsInjectionModule.createInjector();
    IndexManager indexManager = injector.getInstance(IndexManager.class);
    Repository repository = injector.getInstance(Repository.class);

    VREs vres = injector.getInstance(VREs.class);

    if (args.length <= 0) {
      System.out
        .println("VRE id expected choose from " + vres.getAll().stream().map(vre -> vre.getVreId()).collect(toList()));
      shutdown(indexManager, repository);
      return;
    }

    String vreId = args[0];
    VRE vre = vres.getVREById(vreId);

    vre.clearIndexes();

    int numberOfTasks = vre.getEntityTypes().size();
    int numberOfProcessors = Runtime.getRuntime().availableProcessors();
    LOG.info("Indexing indexing for vre {}, {} collections, using {} processes", vreId, numberOfTasks,
      numberOfProcessors);

    CountDownLatch countDownLatch = new CountDownLatch(numberOfTasks);
    ExecutorService executor = Executors.newFixedThreadPool(numberOfProcessors);

    for (Class<? extends DomainEntity> type : vre.getEntityTypes()) {
      executor.execute(new Indexer(type, repository, indexManager, countDownLatch));
    }

    executor.shutdown();
    countDownLatch.await(); // wait until all tasks are completed
    shutdown(indexManager, repository);
    LOG.info("Time used: {}", stopwatch);
  }

  private static void shutdown(IndexManager indexManager, Repository repository) {
    indexManager.close();
    repository.close();
  }


}

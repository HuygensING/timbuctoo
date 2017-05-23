package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.ProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.ObjectMapperFactory;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class BdbCollectionStoreMaker {

  public static BdbCollectionIndex collectionIndex(ListMultimap<String, String> items)
    throws DatabaseException, ProcessingFailedException {
    final EnvironmentConfig configuration = new EnvironmentConfig(new Properties());
    configuration.setAllowCreate(true);
    configuration.setTransactional(true);

    File dir = Files.createTempDir();
    Environment dataSetEnvironment = new Environment(dir, configuration);

    BdbCollectionIndex store =
      new BdbCollectionIndex("collectionIndex-" + UUID.randomUUID(), dataSetEnvironment, new ObjectMapperFactory());

    store.process(handler -> {
      int index = 0;
      for (Map.Entry<String, String> entry : items.entries()) {
        handler.onRelation(index++, entry.getValue(), RDF_TYPE, entry.getKey(), null);
      }
    }, 1);


    return store;
  }
}

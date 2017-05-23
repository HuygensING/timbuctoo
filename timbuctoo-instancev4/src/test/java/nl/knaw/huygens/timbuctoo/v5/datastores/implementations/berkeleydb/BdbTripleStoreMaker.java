package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import com.google.common.io.Files;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.ProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.ObjectMapperFactory;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class BdbTripleStoreMaker {

  public static CloseableTempStore make(List<Quad> quads) throws DatabaseException, ProcessingFailedException {
    final EnvironmentConfig configuration = new EnvironmentConfig(new Properties());
    configuration.setAllowCreate(true);

    File dir = Files.createTempDir();
    Environment dataSetEnvironment = new Environment(dir, configuration);

    BdbTripleStore bdbTripleStore =
      new BdbTripleStore("TripleStore-" + UUID.randomUUID(), dataSetEnvironment, new ObjectMapperFactory());

    bdbTripleStore.process(handler -> {
      handler.start(quads.size());
      for (int i = 0; i < quads.size(); i++) {
        final Quad quad = quads.get(i);
        handler.onQuad(
          i,
          quad.getSubject(),
          quad.getPredicate(),
          quad.getObject(),
          quad.getValuetype().orElse(null),
          quad.getLanguage().orElse(null),
          quad.getGraph()
        );
      }
      handler.finish();
    }, 1);
    return new CloseableTempStore(dataSetEnvironment, bdbTripleStore, dir);
  }


  public static class CloseableTempStore implements AutoCloseable {

    private final Environment environment;

    private final BdbTripleStore store;
    private final File dir;

    private CloseableTempStore(Environment environment, BdbTripleStore store, File dir) {
      this.environment = environment;
      this.store = store;
      this.dir = dir;
    }

    @Override
    public void close() throws Exception {
      store.close();
      environment.close();
      dir.delete();
    }

    public BdbTripleStore getStore() {
      return store;
    }
  }
}

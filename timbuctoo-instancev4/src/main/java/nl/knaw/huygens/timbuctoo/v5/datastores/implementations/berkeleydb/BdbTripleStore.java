package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatus;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatusImpl;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadLoader;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.ProcessingFailedException;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.LANGSTRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

public class BdbTripleStore extends BerkeleyStore implements TripleStore {

  protected TripleWriter tripleWriter;
  private static final Logger LOG = getLogger(BdbTripleStore.class);

  public BdbTripleStore(String dataSetName, Environment dbEnvironment, ObjectMapper objectMapper)
      throws DatabaseException {
    super(dbEnvironment, "rdfData_" + dataSetName, objectMapper);
  }

  protected DatabaseConfig getDatabaseConfig() {
    DatabaseConfig rdfConfig = new DatabaseConfig();
    rdfConfig.setAllowCreate(true);
    rdfConfig.setSortedDuplicates(true);
    return rdfConfig;
  }

  @Override
  public void getQuads(QuadHandler handler) throws LogProcessingFailedException {
    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry value = new DatabaseEntry();

    BerkeleyStore.DatabaseFunction getNext = cursor -> cursor.getNext(key, value, LockMode.DEFAULT);
    try (Stream<Quad> items = getItems(getNext, getNext, () -> formatResult(key, value))) {
      processItems(handler, items);
    }
  }

  @Override
  public Stream<Quad> getQuads(String subject) {

    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry value = new DatabaseEntry();
    String terminatedSubject = subject + "\n";

    binding.objectToEntry(terminatedSubject, key);

    return getItems(
      cursor -> cursor.getSearchKeyRange(key, value, LockMode.DEFAULT),
      cursor -> {
        OperationStatus result = cursor.getNext(key, value, LockMode.DEFAULT);
        if (result == OperationStatus.SUCCESS) {
          String newKey = binding.entryToObject(key);
          if (!newKey.startsWith(terminatedSubject)) {
            return OperationStatus.NOTFOUND;
          }
        }
        return result;
      },
      () -> formatResult(key, value)
    );
  }

  @Override
  public Stream<Quad> getQuads(String subject, String predicate) {

    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry value = new DatabaseEntry();

    if (predicate.equals(RDF_TYPE)) {
      predicate = "";
    }
    binding.objectToEntry(subject + "\n" + predicate, key);
    value.setData(new byte[0]);

    return getItems(
      cursor -> cursor.getSearchKey(key, value, LockMode.DEFAULT),
      cursor -> cursor.getNextDup(key, value, LockMode.DEFAULT),
      () -> formatResult(key, value)
    );
  }

  @Override
  public Optional<Quad> getFirst(String subject, String predicate) {
    try {
      return getItem(subject + "\n" + predicate)
        .map(val -> makeQuad(new String[] {subject, predicate}, val.split("\n")));
    } catch (DatabaseException e) {
      LOG.error("Could not retrieve item " + subject + ", " + predicate, e);
      return Optional.empty();
    }
  }

  private void processItems(QuadHandler handler, Stream<Quad> items)
      throws LogProcessingFailedException {
    boolean ok = true;
    handler.start(0);
    long line = 0;
    Iterator<Quad> iterator = items.iterator();
    while (ok && iterator.hasNext()) {
      if (Thread.currentThread().isInterrupted()) {
        handler.cancel();
        ok = false;
      }
      Quad qd = iterator.next();
      if (qd.getLanguage().isPresent()) {
        handler.onLanguageTaggedString(
          line++,
          qd.getSubject(),
          qd.getPredicate(),
          qd.getObject(),
          qd.getLanguage().get(),
          qd.getGraph()
        );
      } else if (qd.getValuetype().isPresent()) {
        handler.onLiteral(
          line++,
          qd.getSubject(),
          qd.getPredicate(),
          qd.getObject(),
          qd.getValuetype().get(),
          qd.getGraph()
        );
      } else {
        handler.onRelation(line++, qd.getSubject(), qd.getPredicate(), qd.getObject(), qd.getGraph());
      }
    }
    handler.finish();
  }

  private Quad formatResult(DatabaseEntry key, DatabaseEntry value) {
    String[] keyFields = binding.entryToObject(key).split("\n");
    String[] valueFields = binding.entryToObject(value).split("\n", 2);
    return makeQuad(keyFields, valueFields);
  }

  private Quad makeQuad(String[] keyFields, String[] valueFields) {
    return Quad.create(
      keyFields[0],
      keyFields.length == 1 ? RDF_TYPE : keyFields[1],
      valueFields[1],
      valueFields[0].isEmpty() ? null : valueFields[0],
      valueFields[0].startsWith(LANGSTRING) ? valueFields[0].substring(LANGSTRING.length() + 1) : null,
      "http://Notsupported"
    );
  }

  @Override
  public StoreStatus getStatus() {
    return storeStatus;
  }

  @Override
  public void process(QuadLoader source, long version) throws ProcessingFailedException {
    try {
      tripleWriter = new TripleWriter(this, storeStatus, version);
      source.sendQuads(tripleWriter);
    } catch (LogProcessingFailedException e) {
      throw new ProcessingFailedException(e);
    }
  }

  private class TripleWriter extends TripleWriterBase {
    public TripleWriter(BdbTripleStore store, StoreStatusImpl storeStatus, long newVersion) {
      super(store, storeStatus, newVersion);
    }

    @Override
    public void onRelation(long line, String subject, String predicate, String object, String graph)
        throws LogProcessingFailedException {
      if (storeStatus.getPosition() > line) {
        return;
      }
      if (predicate.equals(RDF_TYPE)) {
        predicate = "";
      }
      try {
        put(subject + "\n" + predicate, "\n" + object);
        if (!predicate.isEmpty()) {
          put(
            object + "\n" +
              predicate + "_inverse",//FIXME! maybe make this a marker or something? instead of creating a new predicate
            "\n" + subject
          );
        }
        storeStatus.setPosition(line);
      } catch (DatabaseException e) {
        throw new LogProcessingFailedException(e);
      }
    }

    @Override
    public void onLiteral(long line, String subject, String predicate, String object, String valueType, String graph)
        throws LogProcessingFailedException {
      if (storeStatus.getPosition() > line) {
        return;
      }

      try {
        put(subject + "\n" + predicate, valueType + "\n" + object);
        storeStatus.setPosition(line);
      } catch (DatabaseException e) {
        throw new LogProcessingFailedException(e);
      }
    }

    @Override
    public void onLanguageTaggedString(long line, String subject, String predicate, String value, String language,
                                       String graph)
        throws LogProcessingFailedException {
      if (storeStatus.getPosition() > line) {
        return;
      }
      try {
        put(subject + "\n" + predicate, LANGSTRING + "-" + language + "\n" +
          value); //FIXME! store languages properly
        storeStatus.setPosition(line);
      } catch (DatabaseException e) {
        throw new LogProcessingFailedException(e);
      }
    }

  }
}

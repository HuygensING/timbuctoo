package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.sleepycat.je.DatabaseConfig;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.DatabaseGetter;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.EntityProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.EntityProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PredicateData;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.RelationPredicate;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.ValuePredicate;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.LANGSTRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

public class BdbTripleStore extends BerkeleyStore implements EntityProvider, QuadStore {

  private static final Logger LOG = getLogger(BdbTripleStore.class);

  public BdbTripleStore(DataProvider dataProvider, BdbDatabaseCreator dbFactory, String userId, String datasetId)
    throws DataStoreCreationException {
    super(dbFactory, "rdfData", userId, datasetId);
    dataProvider.subscribeToRdf(this);
  }

  protected DatabaseConfig getDatabaseConfig() {
    DatabaseConfig rdfConfig = new DatabaseConfig();
    rdfConfig.setSortedDuplicates(true);
    rdfConfig.setAllowCreate(true);
    rdfConfig.setDeferredWrite(true);
    return rdfConfig;
  }

  @Override
  public Stream<CursorQuad> getQuads(String subject, String predicate, Direction direction, String cursor) {
    final DatabaseGetter<String, String> getter;
    if (cursor.isEmpty()) {
      getter = bdbWrapper.databaseGetter()
        .startAtKey((subject + "\n" + predicate + "\n" + direction.name()))
        .getAllWithSameKey(true);
    } else {
      if (cursor.equals("LAST")) {
        getter = bdbWrapper.databaseGetter()
          .startAtEndOfKeyDuplicates((subject + "\n" + predicate + "\n" + direction.name()))
          .getAllWithSameKey(false);
      } else {
        String[] fields = cursor.substring(2).split("\n");
        getter = bdbWrapper.databaseGetter()
          .startAfterValue(
            fields[0] + "\n" + fields[1] + "\n" + fields[2],
            fields[3] + "\n" + fields[4] + "\n" + fields[5]
          )
          .getAllWithSameKey(cursor.startsWith("A\n"));
      }
    }
    return getter.getKeysAndValues(this::formatResult);
  }

  private CursorQuad formatResult(String key, String value) {
    String cursor = key + "\n" + value;
    String[] keyFields = cursor.split("\n", 6);
    return CursorQuad.create(
      keyFields[0],
      keyFields[1],
      Direction.valueOf(keyFields[2]),
      keyFields[5],
      keyFields[3].isEmpty() ? null : keyFields[3],
      keyFields[4].isEmpty() ? null : keyFields[4],
      cursor
    );
  }

  @Override
  public void setPrefix(String prefix, String iri) throws RdfProcessingFailedException {}

  private void putQuad(String subject, String predicate, Direction direction, String dataType, String language,
                       String object) throws RdfProcessingFailedException {
    try {
      String value = (dataType == null ? "" : dataType) + "\n" + (language == null ? "" : language) + "\n" + object;
      bdbWrapper.put(subject + "\n" + predicate + "\n" + direction.name(), value);
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e.getCause());
    }
  }

  private void deleteQuad(String subject, String predicate, Direction direction, String dataType, String language,
                          String object) throws RdfProcessingFailedException {
    try {
      String value = (dataType == null ? "" : dataType) + "\n" + (language == null ? "" : language) + "\n" + object;
      bdbWrapper.delete(subject + "\n" + predicate + "\n" + direction.name(), value);
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e.getCause());
    }
  }

  @Override
  public void addRelation(String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
    putQuad(subject, predicate, Direction.OUT, null, null, object);
    putQuad(object, predicate, Direction.IN, null, null, subject);
  }

  @Override
  public void addValue(String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException {
    putQuad(subject, predicate, Direction.OUT, dataType, null, value);
  }

  @Override
  public void addLanguageTaggedString(String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {
    putQuad(subject, predicate, Direction.OUT, LANGSTRING, language, value);
  }

  @Override
  public void delRelation(String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
    deleteQuad(subject, predicate, Direction.OUT, null, null, object);
    deleteQuad(object, predicate, Direction.IN, null, null, subject);
  }

  @Override
  public void delValue(String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException {
    deleteQuad(subject, predicate, Direction.OUT, dataType, null, value);
  }

  @Override
  public void delLanguageTaggedString(String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {
    deleteQuad(subject, predicate, Direction.OUT, LANGSTRING, language, value);
  }

  @Override
  public void processEntities(EntityProcessor processor, int index) throws RdfProcessingFailedException {
    ListMultimap<String, PredicateData> predicates = MultimapBuilder.hashKeys().arrayListValues().build();
    Map<String, Boolean> inversePredicates = new HashMap<>();
    String curSubject = "";
    processor.start(index);

    Stopwatch stopwatch = Stopwatch.createStarted();
    int prevTripleCount = 0;
    int tripleCount = 0;
    int subjectCount = 0;

    try (Stream<CursorQuad> quadStream = bdbWrapper.databaseGetter().getAll().getKeysAndValues(this::formatResult)) {
      Iterator<CursorQuad> quads = quadStream.iterator();
      while (quads.hasNext()) {
        tripleCount++;
        long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
        if (elapsed > 5) {
          LOG.info("Processed " + tripleCount + " triples and " + subjectCount + " subjects (" +
            ((tripleCount - prevTripleCount) / (elapsed == 0 ? 1 : elapsed)) + " triples/s)" );
          stopwatch.reset();
          stopwatch.start();
          prevTripleCount = tripleCount;
        }
        CursorQuad quad = quads.next();
        if (!curSubject.equals(quad.getSubject())) {
          subjectCount++;
          if (curSubject.length() > 0) {
            processor.processEntity("", curSubject, predicates, inversePredicates);
          }
          curSubject = quad.getSubject();
          predicates.clear();
          inversePredicates.clear();
        }
        String predicate = quad.getPredicate();
        if (quad.getDirection() == Direction.IN) {
          if (inversePredicates.containsKey(predicate)) { //if we encounter it more then once
            inversePredicates.put(predicate, true);
          } else {
            inversePredicates.put(predicate, false);
          }
        } else {
          if (quad.getValuetype().isPresent()) {
            predicates.put(
              predicate,
              new ValuePredicate(predicate, quad.getObject(), quad.getValuetype().get())
            );
          } else {
            try (Stream<CursorQuad> objectTypes = this.getQuads(quad.getObject(), RDF_TYPE, Direction.OUT, "")) {
              List<String> types = objectTypes
                .map(CursorQuad::getObject)
                .collect(Collectors.toList());
              predicates.put(predicate, new RelationPredicate(predicate, quad.getObject(), types));
            }
          }
        }
      }
    }
    if (curSubject.length() > 0) {
      processor.processEntity("", curSubject, predicates, inversePredicates);
    }
    long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
    LOG.info("Done. Processed " + tripleCount + " triples and " + subjectCount + " subjects (" +
      ((tripleCount - prevTripleCount) / (elapsed == 0 ? 1 : elapsed)) + " triples/s)" );

    processor.finish();
  }

}

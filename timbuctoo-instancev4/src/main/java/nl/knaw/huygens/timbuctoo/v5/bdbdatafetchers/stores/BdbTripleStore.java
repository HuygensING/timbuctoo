package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import nl.knaw.huygens.timbuctoo.v5.bdb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.bdb.DatabaseFunction;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.EntityProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.EntityProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.PredicateData;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.RelationPredicate;
import nl.knaw.huygens.timbuctoo.v5.dataset.ValuePredicate;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

public class BdbTripleStore extends BerkeleyStore implements EntityProvider, QuadStore {

  private static final Logger LOG = getLogger(BdbTripleStore.class);

  public BdbTripleStore(DataProvider dataProvider, BdbDatabaseCreator dbFactory, String userId, String datasetId)
    throws DataStoreCreationException {
    super(dbFactory, "rdfData", userId, datasetId);
    dataProvider.subscribeToRdf(this, null);
  }

  protected DatabaseConfig getDatabaseConfig() {
    DatabaseConfig rdfConfig = new DatabaseConfig();
    rdfConfig.setSortedDuplicates(true);
    rdfConfig.setAllowCreate(true);
    rdfConfig.setDeferredWrite(true);
    return rdfConfig;
  }

  @Override
  public Stream<CursorQuad> getQuads(String subject, String predicate, String cursor) {
    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry value = new DatabaseEntry();

    DatabaseFunction iterateForwards = dbCursor -> dbCursor.getNextDup(key, value, LockMode.DEFAULT);
    DatabaseFunction iterateBackwards = dbCursor -> dbCursor.getPrevDup(key, value, LockMode.DEFAULT);

    DatabaseFunction initializer;
    DatabaseFunction iterator;
    if (cursor.isEmpty()) {
      binder.objectToEntry((subject + "\n" + predicate), key);
      initializer = dbCursor -> dbCursor.getSearchKey(key, value, LockMode.DEFAULT);
      iterator = iterateForwards;
    } else {
      if (cursor.equals("LAST")) {
        binder.objectToEntry((subject + "\n" + predicate), key);
        initializer = dbCursor -> {
          OperationStatus status = dbCursor.getSearchKey(key, value, LockMode.DEFAULT);
          if (status == OperationStatus.SUCCESS) {
            status = dbCursor.getNextNoDup(key, value, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
              return dbCursor.getPrev(key, value, LockMode.DEFAULT);
            } else {
              return dbCursor.getLast(key, value, LockMode.DEFAULT);
            }
          } else {
            return status;
          }
        };
        iterator = iterateBackwards;
      } else {
        String[] fields = cursor.substring(2).split("\n");
        binder.objectToEntry(fields[0] + "\n" + fields[1], key);
        binder.objectToEntry(fields[2] + "\n" + fields[3] + "\n" + fields[4], value);
        if (cursor.startsWith("D\n")) {
          iterator = iterateBackwards;
        } else {
          iterator = iterateForwards;
        }
        initializer = dbCursor -> {
          OperationStatus status = dbCursor.getSearchBoth(key, value, LockMode.DEFAULT);
          if (status == OperationStatus.SUCCESS) {
            return iterator.apply(dbCursor);
          }
          return status;
        };
      }
    }
    return getItems(
      initializer,
      iterator,
      () -> formatResult(key, value)
    );
  }

  private CursorQuad formatResult(DatabaseEntry key, DatabaseEntry value) {
    String cursor = binder.entryToObject(key) + "\n" + binder.entryToObject(value);
    String[] keyFields = cursor.split("\n", 5);
    return CursorQuad.create(
      keyFields[0],
      keyFields[1],
      keyFields[4],
      keyFields[2].isEmpty() ? null : keyFields[2],
      keyFields[3].isEmpty() ? null : keyFields[3],
      cursor
    );
  }

  @Override
  public void setPrefix(String cursor, String prefix, String iri) throws RdfProcessingFailedException {}

  @Override
  public void addRelation(String cursor, String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
    new RelationDatabaseAction() {

      @Override
      public void executeAction(String key, String value) throws DatabaseException {
        put(key, value);
      }
    }.execute(subject, predicate, object);
  }

  @Override
  public void addValue(String cursor, String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException {
    try {
      put(subject + "\n" +
        predicate,
        dataType + "\n" +
        /*language*/ "\n" +
        value
      );
    } catch (DatabaseException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public void addLanguageTaggedString(String cursor, String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {
    try {
      put(subject + "\n" +
          predicate,
          RdfConstants.LANGSTRING + "\n" +
          language + "\n" +
          value
      );
    } catch (DatabaseException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public void delRelation(String cursor, String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {

    new RelationDatabaseAction(){
      @Override
      public void executeAction(String key, String value) {
        delete(key, value);
      }
    }.execute(subject, predicate, object);
  }

  @Override
  public void delValue(String cursor, String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException {
    try {
      delete(subject + "\n" +
          predicate,
        dataType + "\n" +
      /*language*/ "\n" +
          value
      );
    } catch (DatabaseException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public void delLanguageTaggedString(String cursor, String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {
    try {
      delete(subject + "\n" +
          predicate,
        RdfConstants.LANGSTRING + "\n" +
          language + "\n" +
          value
      );
    } catch (DatabaseException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public void processEntities(String cursor, EntityProcessor processor) throws RdfProcessingFailedException {
    ListMultimap<String, PredicateData> predicates = MultimapBuilder.hashKeys().arrayListValues().build();
    Map<String, Boolean> inversePredicates = new HashMap<>();
    String curSubject = "";
    processor.start();

    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry value = new DatabaseEntry();
    DatabaseFunction getNext = dbCursor -> dbCursor.getNext(key, value, LockMode.DEFAULT);

    Stopwatch stopwatch = Stopwatch.createStarted();
    int prevTripleCount = 0;
    int tripleCount = 0;
    int subjectCount = 0;
    try (Stream<CursorQuad> quadStream = getItems(getNext, getNext, () -> formatResult(key, value))) {
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
          processor.processEntity("", curSubject, predicates, inversePredicates);
          curSubject = quad.getSubject();
          predicates.clear();
          inversePredicates.clear();
        }
        String predicate = quad.getPredicate();
        if (predicate.endsWith("_inverse")) {
          String origPredicateName = predicate.substring(0, predicate.length() - "_inverse".length());
          if (inversePredicates.containsKey(origPredicateName)) { //if we encounter it more then once
            inversePredicates.put(origPredicateName, true);
          } else {
            inversePredicates.put(origPredicateName, false);
          }
        } else {
          if (quad.getValuetype().isPresent()) {
            predicates.put(
              predicate,
              new ValuePredicate(predicate, quad.getObject(), quad.getValuetype().get())
            );
          } else {
            try (Stream<CursorQuad> objectTypes = this.getQuads(quad.getObject(), RDF_TYPE, "")) {
              List<String> types = objectTypes
                .map(CursorQuad::getObject)
                .collect(Collectors.toList());
              predicates.put(predicate, new RelationPredicate(predicate, quad.getObject(), types));
            }
          }
        }
      }
    }
    long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
    LOG.info("Done. Processed " + tripleCount + " triples and " + subjectCount + " subjects (" +
      ((tripleCount - prevTripleCount) / (elapsed == 0 ? 1 : elapsed)) + " triples/s)" );

    processor.finish();
  }

  private abstract class RelationDatabaseAction {
    public void execute(String subject, String predicate, String object) throws RdfProcessingFailedException {
      try {
        executeAction(subject + "\n" +
            predicate,
        /*dataType*/ "\n" +
        /*language*/ "\n" +
            object
        );
        if (!predicate.equals(RDF_TYPE)) {
          executeAction(object + "\n" +
              predicate + "_inverse",//FIXME!
            /*dataType*/ "\n" +
            /*language*/ "\n" +
              subject
          );
        }
      } catch (DatabaseException e) {
        throw new RdfProcessingFailedException(e);
      }
    }

    public abstract void executeAction(String key, String value) throws DatabaseException;
  }
}

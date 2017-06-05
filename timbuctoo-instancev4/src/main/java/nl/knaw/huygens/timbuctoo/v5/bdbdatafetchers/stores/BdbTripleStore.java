package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import nl.knaw.huygens.timbuctoo.v5.bdb.DatabaseFunction;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.EntityProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.EntityProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.PredicateData;
import nl.knaw.huygens.timbuctoo.v5.dataset.RelationPredicate;
import nl.knaw.huygens.timbuctoo.v5.dataset.ValuePredicate;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.bdb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class BdbTripleStore extends BerkeleyStore implements EntityProvider {

  public BdbTripleStore(DataProvider dataProvider, BdbDatabaseCreator dbFactory, String userId, String datasetId)
    throws DataStoreCreationException {
    super(dbFactory, "rdfData", userId, datasetId);
    dataProvider.subscribeToRdf(this, null);
  }

  protected DatabaseConfig getDatabaseConfig() {
    DatabaseConfig rdfConfig = new DatabaseConfig();
    rdfConfig.setSortedDuplicates(true);
    rdfConfig.setAllowCreate(true);
    return rdfConfig;
  }

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
    try {
      put(subject + "\n" +
        predicate,
        /*dataType*/ "\n" +
        /*language*/ "\n" +
        object
      );
      if (!predicate.equals(RDF_TYPE)) {
        put(object + "\n" +
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
      throws RdfProcessingFailedException {}

  @Override
  public void delValue(String cursor, String subject, String predicate, String value, String valueType, String graph)
      throws RdfProcessingFailedException {}

  @Override
  public void delLanguageTaggedString(String cursor, String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {}

  @Override
  public void processEntities(String cursor, EntityProcessor processor) throws RdfProcessingFailedException {
    ListMultimap<String, PredicateData> predicates = MultimapBuilder.hashKeys().arrayListValues().build();
    String curSubject = "";
    processor.start();

    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry value = new DatabaseEntry();
    DatabaseFunction getNext = dbCursor -> dbCursor.getNext(key, value, LockMode.DEFAULT);

    try (Stream<CursorQuad> quadStream = getItems(getNext, getNext, () -> formatResult(key, value))) {
      Iterator<CursorQuad> quads = quadStream.iterator();
      while (quads.hasNext()) {
        CursorQuad quad = quads.next();
        if (!curSubject.equals(quad.getSubject())) {
          processor.processEntity("", curSubject, predicates);
          curSubject = quad.getSubject();
          predicates.clear();
        }
        if (quad.getValuetype().isPresent()) {
          predicates.put(
            quad.getPredicate(),
            new ValuePredicate(quad.getPredicate(), quad.getObject(), quad.getValuetype().get())
          );
        } else {
          try (Stream<CursorQuad> objectTypes = this.getQuads(quad.getObject(), RDF_TYPE, "")) {
            List<String> types = objectTypes
              .map(CursorQuad::getObject)
              .collect(Collectors.toList());
            predicates.put(quad.getPredicate(), new RelationPredicate(quad.getPredicate(), quad.getObject(), types));
          }
        }
      }
    }
    processor.finish();
  }
}

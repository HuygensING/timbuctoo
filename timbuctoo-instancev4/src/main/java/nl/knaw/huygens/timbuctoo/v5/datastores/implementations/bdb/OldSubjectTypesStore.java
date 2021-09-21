package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.DatabaseGetter;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.updatedperpatchstore.SubjectCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Stream;

public class OldSubjectTypesStore {
  private static final Logger LOG = LoggerFactory.getLogger(OldSubjectTypesStore.class);
  private final BdbWrapper<String, String> bdbWrapper;

  public OldSubjectTypesStore(BdbWrapper<String, String> bdbWrapper) throws DataStoreCreationException {
    this.bdbWrapper = bdbWrapper;
  }

  public void put(String subject, String type, int version) throws DatabaseWriteException {
    bdbWrapper.put(type, version + "\n" + subject);
  }

  public void delete(String subject, String type, int version) throws DatabaseWriteException {
    bdbWrapper.delete(type, version + "\n" + subject);
  }

  public Stream<SubjectCursor> fromTypeAndVersion(String type, int version, String cursor) {
    final DatabaseGetter<String, String> getter;
    if (cursor.isEmpty()) {
      getter = bdbWrapper.databaseGetter()
                         .key(type)
                         .skipNearValue(String.valueOf(version))
                         .allValues()
                         .forwards();
    } else {
      String[] fields = cursor.substring(2).split("\n\n", 2);
      getter = bdbWrapper.databaseGetter()
                         .key(type)
                         .skipToValue(fields[0] + "\n" + fields[1])
                         .skipOne()
                         .forwards();
    }
    return getter.getValues(bdbWrapper.valueRetriever()).map(OldSubjectTypesStore::makeSubjectCursor);
  }

  public void close() {
    try {
      bdbWrapper.close();
    } catch (Exception e) {
      LOG.error("Exception closing OldSubjectTypesStore", e);
    }
  }

  public void commit() {
    bdbWrapper.commit();
  }

  public void start() {
    bdbWrapper.beginTransaction();
  }

  public boolean isClean() {
    return bdbWrapper.isClean();
  }

  public void empty() {
    bdbWrapper.empty();
  }

  private static SubjectCursor makeSubjectCursor(String versionAndSubject) {
    String[] fields = versionAndSubject.split("\n", 2);
    return SubjectCursor.create(fields[1], Set.of(Integer.valueOf(fields[0])), fields[0] + "\n\n" + fields[1]);
  }
}

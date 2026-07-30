package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.datastores.oldsubjecttypesstore.OldSubjectTypesStore;
import nl.knaw.huygens.timbuctoo.datastores.updatedperpatchstore.SubjectCursor;
import nl.knaw.huygens.timbuctoo.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.berkeleydb.DatabaseGetter;
import nl.knaw.huygens.timbuctoo.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataStoreCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Stream;

class BdbOldSubjectTypesStore implements OldSubjectTypesStore {
  private static final Logger LOG = LoggerFactory.getLogger(BdbOldSubjectTypesStore.class);
  private final BdbWrapper<String, String> bdbWrapper;

  BdbOldSubjectTypesStore(BdbWrapper<String, String> bdbWrapper) throws DataStoreCreationException {
    this.bdbWrapper = bdbWrapper;
  }

  @Override
  public long size() {
    return bdbWrapper.count();
  }

  @Override
  public void put(String subject, String type, int version) throws RdfProcessingFailedException {
    try {
      bdbWrapper.put(type, version + "\n" + subject);
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public void delete(String subject, String type, int version) throws RdfProcessingFailedException {
    try {
      bdbWrapper.delete(type, version + "\n" + subject);
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
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
    return getter.getValues(bdbWrapper.valueRetriever()).map(BdbOldSubjectTypesStore::makeSubjectCursor);
  }

  @Override
  public void close() {
    try {
      bdbWrapper.close();
    } catch (Exception e) {
      LOG.error("Exception closing OldSubjectTypesStore", e);
    }
  }

  @Override
  public void commit() {
    bdbWrapper.commit();
  }

  @Override
  public void start() {
    bdbWrapper.beginTransaction();
  }

  @Override
  public boolean isClean() {
    return bdbWrapper.isClean();
  }

  @Override
  public void empty() {
    bdbWrapper.empty();
  }

  private static SubjectCursor makeSubjectCursor(String versionAndSubject) {
    String[] fields = versionAndSubject.split("\n", 2);
    return SubjectCursor.create(fields[1], Set.of(Integer.valueOf(fields[0])), fields[0] + "\n\n" + fields[1]);
  }
}

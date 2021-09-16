package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.updatedperpatchstore.SubjectCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class UpdatedPerPatchStore {
  private static final Logger LOG = LoggerFactory.getLogger(UpdatedPerPatchStore.class);
  private final BdbWrapper<Integer, String> bdbWrapper;

  public UpdatedPerPatchStore(BdbWrapper<Integer, String> bdbWrapper) throws DataStoreCreationException {
    this.bdbWrapper = bdbWrapper;
  }

  public void put(int version, String subject) throws DatabaseWriteException {
    bdbWrapper.put(version, subject);
  }

  public void delete(int version, String subject) throws DatabaseWriteException {
    bdbWrapper.delete(version, subject);
  }

  public Stream<String> ofVersion(int version) {
    return bdbWrapper.databaseGetter().key(version).dontSkip().forwards().getValues(bdbWrapper.valueRetriever());
  }

  public Stream<SubjectCursor> fromVersion(int version, String cursor, Predicate<String> subjectFilter) {
    String startSubject = null;
    int cursorVersion = -1;
    if (!cursor.isEmpty()) {
      String[] fields = cursor.substring(2).split("\n", 2);
      cursorVersion = Integer.parseInt(fields[0]);
      startSubject = fields[1];
    }

    int startVersion = Math.max(cursorVersion, version);
    if (startSubject != null) {
      return bdbWrapper.databaseGetter()
                       .skipToKey(startVersion)
                       .skipToValue(startSubject)
                       .skipOne()
                       .forwards()
                       .getKeysAndValues(bdbWrapper.keyValueConverter(UpdatedPerPatchStore::makeSubjectCursor))
                       .filter(subjectCursor -> subjectFilter.test(subjectCursor.getSubject()));
    }

    return bdbWrapper.databaseGetter()
                     .skipToKey(startVersion)
                     .dontSkip()
                     .forwards()
                     .getKeysAndValues(bdbWrapper.keyValueConverter(UpdatedPerPatchStore::makeSubjectCursor))
                     .filter(subjectCursor -> subjectFilter.test(subjectCursor.getSubject()));
  }

  public Stream<Integer> getVersions() {
    return bdbWrapper.databaseGetter().getAll().getKeys(bdbWrapper.keyRetriever()).distinct();
  }

  public void close() {
    try {
      bdbWrapper.close();
    } catch (Exception e) {
      LOG.error("Exception closing UpdatedPerPatchStore", e);
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

  private static SubjectCursor makeSubjectCursor(int version, String subject) {
    return SubjectCursor.create(subject, String.format("%s\n%s", version, subject));
  }
}

package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.datastores.updatedperpatchstore.SubjectCursor;
import nl.knaw.huygens.timbuctoo.util.Streams;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataStoreCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdatedPerPatchStore {
  private static final Logger LOG = LoggerFactory.getLogger(UpdatedPerPatchStore.class);
  private static final String ALL_VERSIONS_KEY = "allVersions";

  private final BdbWrapper<String, Integer> bdbWrapper;

  public UpdatedPerPatchStore(BdbWrapper<String, Integer> bdbWrapper) throws DataStoreCreationException {
    this.bdbWrapper = bdbWrapper;
  }

  public void put(int version, String subject) throws DatabaseWriteException {
    bdbWrapper.put(subject, version);
    bdbWrapper.put(ALL_VERSIONS_KEY, version);
  }

  public Stream<String> ofVersion(int version) {
    return bdbWrapper.databaseGetter().getAll()
                     .getKeysAndValues(bdbWrapper.keyValueConverter(Tuple::new))
                     .filter(tuple -> !tuple.left().equals(ALL_VERSIONS_KEY))
                     .filter(tuple -> tuple.right() == version)
                     .map(Tuple::left);
  }

  public Stream<SubjectCursor> fromVersion(int version, String cursor) {
    Stream<Tuple<String, Integer>> stream;
    String startSubject = !cursor.isEmpty() ? cursor.substring(2) : null;
    if (startSubject != null) {
      stream = bdbWrapper.databaseGetter()
                         .skipToKey(startSubject)
                         .skipToEnd()
                         .skipOne()
                         .forwards()
                         .getKeysAndValues(bdbWrapper.keyValueConverter(Tuple::new))
                         .filter(tuple -> !tuple.left().equals(ALL_VERSIONS_KEY));
    } else {
      stream = bdbWrapper.databaseGetter().getAll()
                         .getKeysAndValues(bdbWrapper.keyValueConverter(Tuple::new))
                         .filter(tuple -> !tuple.left().equals(ALL_VERSIONS_KEY));
    }

    return Streams.combine(stream, (scA, scB) -> scA.left().equals(scB.left()))
                  .map(UpdatedPerPatchStore::makeSubjectCursor)
                  .filter(subjectCursor -> subjectCursor.getVersions().stream().anyMatch(v -> v >= version));
  }

  public Stream<Integer> getVersions() {
    return bdbWrapper.databaseGetter()
                     .key(ALL_VERSIONS_KEY)
                     .dontSkip()
                     .forwards()
                     .getValues(bdbWrapper.valueRetriever());
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

  private static SubjectCursor makeSubjectCursor(Set<Tuple<String, Integer>> subjectVersions) {
    Optional<String> subject = subjectVersions.stream().findAny().map(Tuple::left);
    Set<Integer> versions = subjectVersions.stream().map(Tuple::right).collect(Collectors.toSet());
    return subject.map(s -> SubjectCursor.create(s, versions)).orElse(null);
  }
}
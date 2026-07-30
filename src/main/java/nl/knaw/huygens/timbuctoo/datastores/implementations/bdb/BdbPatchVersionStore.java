package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.datastores.patchversionstore.PatchVersionStore;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction.IN;
import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction.OUT;

class BdbPatchVersionStore implements PatchVersionStore {
  private static final Logger LOG = LoggerFactory.getLogger(BdbPatchVersionStore.class);
  private final BdbWrapper<String, String> bdbWrapper;

  BdbPatchVersionStore(BdbWrapper<String, String> bdbWrapper) throws DataStoreCreationException {
    this.bdbWrapper = bdbWrapper;
  }

  @Override
  public void put(String subject, String predicate, Direction direction, boolean isAssertion,
                  String object, String valueType, String language, String graph) throws RdfProcessingFailedException {
    try {
      //if we assert something and then retract it in the same patch, it's as if it never happened at all
      //so we delete the inversion
      final String value = predicate + "\n" +
          (direction == OUT ? "1" : "0") + "\n" +
          (valueType == null ? "" : valueType) + "\n" +
          (language == null ? "" : language) + "\n" +
          object + "\n" +
          (graph == null ? "" : graph);

      bdbWrapper.delete(subject + "\n" + (!isAssertion ? 1 : 0), value);
      bdbWrapper.put(subject + "\n" + (isAssertion ? 1 : 0), value);
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public Stream<CursorQuad> getAllChanges(boolean assertions) {
    // FIXME partialKey does not work well with endsWidth, it stops the iterator with the first match
    // See issue T141 on https://github.com/knaw-huc/backlogs/blob/master/structured-data.txt
    return bdbWrapper.databaseGetter()
        .getAll()
        // .partialKey("\n" + (assertions ? "1" : "0"), (pf, key) -> key.endsWith(pf))
        // .dontSkip()
        // .forwards()
        .getKeysAndValues(bdbWrapper.keyValueConverter(Tuple::tuple))
        .filter(kv -> kv.left().endsWith(assertions ? "1" : "0"))
        .map((value) -> makeCursorQuad(value.left().split("\n")[0], assertions, value.right()));
  }

  @Override
  public Stream<CursorQuad> getChanges(String subject, boolean assertions) {
    return bdbWrapper.databaseGetter()
        .key(subject + "\n" + (assertions ? "1" : "0"))
        .dontSkip()
        .forwards()
        .getValues(bdbWrapper.valueRetriever())
        .map(v -> makeCursorQuad(subject, assertions, v));
  }

  @Override
  public Stream<CursorQuad> getChanges(String subject, String predicate, Direction direction, boolean assertions) {
    return bdbWrapper.databaseGetter()
        .key(subject + "\n" + (assertions ? "1" : "0"))
        .skipNearValue(predicate + "\n" + (direction == OUT ? "1" : "0") + "\n")
        .onlyValuesMatching((prefix, value) -> value.startsWith(prefix))
        .forwards()
        .getValues(bdbWrapper.valueRetriever())
        .map(v -> makeCursorQuad(subject, assertions, v));
  }

  @Override
  public Stream<CursorQuad> retrieveChanges() {
    return Stream.concat(getAllChanges(false), getAllChanges(true))
        .filter(quad -> quad.getDirection().equals(Direction.OUT));
  }

  @Override
  public CursorQuad makeCursorQuad(String subject, boolean assertions, String value) {
    String[] parts = value.split("\n", 5);
    Direction direction = parts[1].charAt(0) == '1' ? OUT : IN;
    ChangeType changeType = assertions ? ChangeType.ASSERTED : ChangeType.RETRACTED;
    int objectGraphIdx = parts[4].lastIndexOf('\n');
    return CursorQuad.create(
        subject,
        parts[0],
        direction,
        changeType,
        parts[4].substring(0, objectGraphIdx),
        parts[2].isEmpty() ? null : parts[2],
        parts[3].isEmpty() ? null : parts[3],
        parts[4].substring(objectGraphIdx + 1).isEmpty() ?
            null : parts[4].substring(objectGraphIdx + 1),
        ""
    );
  }

  @Override
  public void close() {
    try {
      bdbWrapper.close();
    } catch (Exception e) {
      LOG.error("Exception closing BdbPatchVersionStore", e);
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
}

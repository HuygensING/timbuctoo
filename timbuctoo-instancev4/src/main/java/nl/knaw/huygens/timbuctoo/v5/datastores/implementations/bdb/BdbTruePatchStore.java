package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.IN;
import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction.OUT;

public class BdbTruePatchStore {

  private static final Logger LOG = LoggerFactory.getLogger(BdbTruePatchStore.class);
  private final BdbWrapper<String, String> bdbWrapper;

  public BdbTruePatchStore(BdbWrapper<String, String> bdbWrapper)
    throws DataStoreCreationException {
    this.bdbWrapper = bdbWrapper;
  }

  public void put(String subject, int currentversion, String predicate, Direction direction, boolean isAssertion,
                  String object, String valueType, String language) throws DatabaseWriteException {
    //if we assert something and then retract it in the same patch, it's as if it never happened at all
    //so we delete the inversion
    final String dirStr = direction == OUT ? "1" : "0";
    bdbWrapper.delete(
      subject + "\n" + currentversion + "\n" + (!isAssertion ? 1 : 0),
      predicate + "\n" +
        dirStr + "\n" +
        (valueType == null ? "" : valueType) + "\n" +
        (language == null ? "" : language) + "\n" +
        object
    );
    bdbWrapper.put(
      subject + "\n" + currentversion + "\n" + (isAssertion ? 1 : 0),
      predicate + "\n" +
        dirStr + "\n" +
        (valueType == null ? "" : valueType) + "\n" +
        (language == null ? "" : language) + "\n" +
        object
    );
  }

  public Stream<CursorQuad> getChanges(String subject, int version, boolean assertions) {
    return bdbWrapper.databaseGetter()
      .key(subject + "\n" + version + "\n" + (assertions ? "1" : "0"))
      .dontSkip()
      .forwards()
      .getValues().map(v -> makeCursorQuad(subject, assertions, v));
  }

  public Stream<CursorQuad> getChanges(String subject, String predicate, Direction direction, int version,
                                       boolean assertions) {
    return bdbWrapper.databaseGetter()
                     .key(subject + "\n" + version + "\n" + (assertions ? "1" : "0"))
                     .skipNearValue(predicate + "\n" + (direction == OUT ? "1" : "0") + "\n")
                     .onlyValuesMatching((prefix, value) -> value.startsWith(prefix))
                     .forwards()
                     .getValues().map(v -> makeCursorQuad(subject, assertions, v));
  }

  public CursorQuad makeCursorQuad(String subject, boolean assertions, String value) {
    String[] parts = value.split("\n", 5);
    Direction direction = parts[1].charAt(0) == '1' ? OUT : IN;
    ChangeType changeType = assertions ? ChangeType.ASSERTED :  ChangeType.RETRACTED;
    return CursorQuad.create(
      subject,
      parts[0],
      direction,
      changeType,
      parts[4],
      parts[2].isEmpty() ? null : parts[2],
      parts[3].isEmpty() ? null : parts[3],
      ""
    );
  }

  public void close() {
    try {
      bdbWrapper.close();
    } catch (Exception e) {
      LOG.error("Exception closing BdbTruePatchStore", e);
    }
  }
}

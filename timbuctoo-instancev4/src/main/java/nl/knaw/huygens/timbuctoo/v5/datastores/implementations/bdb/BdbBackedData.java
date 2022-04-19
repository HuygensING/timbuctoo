package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A simple wrapper around serialized json so that we get the benefits of berkeley db for transactions
 * without having to change the code too much.
 */
public class BdbBackedData implements DataStorage {
  private final BdbWrapper<String, String> bdbWrapper;
  private String value;

  public BdbBackedData(BdbWrapper<String, String> bdbWrapper) {
    this.bdbWrapper = bdbWrapper;

    try (Stream<String> stream = this.bdbWrapper.databaseGetter().getAll().getValues(bdbWrapper.valueRetriever())) {
      final Optional<String> storedSchema = stream.filter(value -> !Objects.equals(value, "isClean")).findAny();
      storedSchema.ifPresent(s -> value = s);
    }
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String newValue) throws DatabaseWriteException {
    value = newValue;
    bdbWrapper.put("value", newValue);
  }

  @Override
  public void close() throws Exception {
    bdbWrapper.close();
  }

  @Override
  public void commit() {
    bdbWrapper.commit();
  }

  @Override
  public void beginTransaction() {
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

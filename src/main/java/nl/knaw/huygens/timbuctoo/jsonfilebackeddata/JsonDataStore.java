package nl.knaw.huygens.timbuctoo.jsonfilebackeddata;

import java.io.IOException;
import java.util.function.Function;

public interface JsonDataStore<T> {
  void updateData(Function<T, T> mutator) throws IOException;

  T getData();
}

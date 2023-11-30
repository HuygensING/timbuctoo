package nl.knaw.huygens.timbuctoo.serializable;

import java.io.IOException;

public interface Serialization {
  void serialize(SerializableResult serializableResult) throws IOException;
}

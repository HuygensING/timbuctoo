package nl.knaw.huygens.timbuctoo.v5.serializable;

import java.io.IOException;

public interface Serialization {
  void serialize(Serializable serializable) throws IOException;
}

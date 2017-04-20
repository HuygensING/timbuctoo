package nl.knaw.huygens.timbuctoo.v5.serializable;

import java.io.IOException;

public interface Serializable {
  void serialize(Serialization serialization) throws IOException;

  void generateToC(ResultToC siblingEntity);

}

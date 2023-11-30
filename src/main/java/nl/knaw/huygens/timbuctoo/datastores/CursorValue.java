package nl.knaw.huygens.timbuctoo.datastores;

import org.immutables.value.Value;

public interface CursorValue {
  @Value.Auxiliary
  String getCursor();
}

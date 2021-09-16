package nl.knaw.huygens.timbuctoo.v5.datastores;

import org.immutables.value.Value;

public interface CursorValue {
  @Value.Auxiliary
  String getCursor();
}

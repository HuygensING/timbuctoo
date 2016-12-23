package nl.knaw.huygens.timbuctoo.core.dto.rdf;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface ValueTypeInUse {
  String getTypeUri();

  /**
   * @return the ids of the entities connected to it
   */
  List<String> getEntitiesConnected();
}

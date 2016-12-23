package nl.knaw.huygens.timbuctoo.core.dto.rdf;

import org.immutables.value.Value;

@Value.Immutable
public interface CreateProperty {
  String getClientName();

  String getTypeUri();

  String getRdfUri();

  String getPropertyType();
}

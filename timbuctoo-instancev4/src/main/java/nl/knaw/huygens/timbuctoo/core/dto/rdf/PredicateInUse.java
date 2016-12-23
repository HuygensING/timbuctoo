package nl.knaw.huygens.timbuctoo.core.dto.rdf;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface PredicateInUse {
  String getPredicateUri();

  List<ValueTypeInUse> getValueTypes();
}

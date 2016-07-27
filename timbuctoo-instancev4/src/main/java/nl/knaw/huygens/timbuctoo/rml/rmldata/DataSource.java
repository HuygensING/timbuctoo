package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.ReferenceGetter;

import java.util.List;
import java.util.Map;

public interface DataSource {

  Iterable<Map<String, Object>> getItems(
    RrLogicalSource rrLogicalSource,
    List<ReferenceGetter> referencingObjectMaps
  );

  void willBeJoinedOn(RrLogicalSource logicalSource, String fieldName, Object referenceJoinValue, String uri);

}

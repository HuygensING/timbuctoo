package nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.Optional;

public interface Saver {
  Vertex setVertexProperties(Collection collection, Optional<String> uniqueValueOpt,
                             HashMap<String, Object> currentProperties);

  Vre getVre();

  Optional<String> makeRelation(Vertex from, String relationName, Collection ownCollection, String otherCollectionName,
                                String value);

  Optional<String> checkLeftoverVerticesThatWereExpected(Collection openCollection);

  void markCollectionAsDone(Collection openCollection);

  Optional<String> checkLeftoverCollectionsThatWereExpected();

  Optional<String> checkRelationtypesThatWereExpected();
}

package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Set;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;

public class TypesHelper {
  public void updateTypeInformation(Vertex vertex, Set<Collection> collections) {
    Stream<String> typesStream =
      collections.stream().map(collection -> collection.getDescription().getEntityTypeName());

    vertex.property("types", jsnA(typesStream.map(JsonBuilder::jsn)).toString());
  }
}

package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.crud.changelistener.AddLabelChangeListener;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;

public class TypesHelper {
  private final AddLabelChangeListener labelChangeListener;

  public TypesHelper() {
    this(new AddLabelChangeListener());
  }

  TypesHelper(AddLabelChangeListener labelChangeListener) {
    this.labelChangeListener = labelChangeListener;
  }

  public void updateTypeInformation(Vertex vertex, Set<Collection> collections) {
    Stream<String> typesStream =
      collections.stream().map(collection -> collection.getDescription().getEntityTypeName());

    vertex.property("types", jsnA(typesStream.map(JsonBuilder::jsn)).toString());
    labelChangeListener.onUpdate(Optional.empty(), vertex);
  }
}

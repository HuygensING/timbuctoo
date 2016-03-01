package nl.knaw.huygens.timbuctoo.model.properties;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.logging.Logmarkers;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.Autocomplete;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.slf4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

public class JsonMetadata {

  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(JsonMetadata.class);


  private final Vres metadata;
  private final GraphWrapper graph;
  private final Map<String, Map<String, String>> keywordTypes;

  public JsonMetadata(Vres metadata, GraphWrapper graph, Map<String, Map<String, String>> keywordTypes) {
    this.metadata = metadata;
    this.graph = graph;
    this.keywordTypes = keywordTypes;
  }

  public ArrayNode getForCollection(Collection collection) {
    ArrayNode result = jsnA();
    collection.getProperties().forEach((name, prop) -> {
      ObjectNode desc = jsnO(
        "name", jsn(name),
        "type", jsn(prop.getGuiTypeId())
      );
      java.util.Collection<String> options = prop.getOptions();
      if (options != null) {
        desc.set("options", jsnA(options.stream().map(JsonBuilder::jsn)));
      }
      result.add(desc);
    });

    //FIXME add check to vres that certifies that the defined derived relations exist in the database
    String abstractType = collection.getAbstractType();
    Vre vre = collection.getVre();
    Map<String, String> keywordTypes = Optional
      .ofNullable(this.keywordTypes.get(vre.getVreName()))
      .orElse(new HashMap<>());

    String relationCollectionName = vre
      .getImplementerOf("relation")
      .map(Collection::getCollectionName)
      .orElseThrow(() -> new RuntimeException("No collections available"));//FIXME: log don't throw

    graph.getGraph().traversal().V()
      .has("relationtype_sourceTypeName", abstractType)
      .forEachRemaining(v -> {

        String timId = getProp(v, "tim_id", String.class).orElse("<unknown>");
        Optional<String> regularName = getProp(v, "relationtype_regularName", String.class);
        Optional<String> inverseName = getProp(v, "relationtype_inverseName", String.class);
        Optional<String> abstractTargetType = getProp(v, "relationtype_targetTypeName", String.class);
        Optional<String> targetType = abstractTargetType
          .flatMap(typeName -> vre.getImplementerOf(typeName).map(Collection::getCollectionName));

        if (regularName.isPresent() && inverseName.isPresent() && targetType.isPresent()) {
          //special support for keywords:
          URI quickSearchUrl;
          if (abstractTargetType.orElse("").equals("keyword")) {
            quickSearchUrl = Autocomplete.makeUrl(
              targetType.get(),
              Optional.empty(),
              Optional.ofNullable(keywordTypes.get(regularName.get()))
            );
          } else {
            quickSearchUrl = Autocomplete.makeUrl(targetType.get());
          }

          result.add(jsnO(
            "name", jsn(regularName.get()),
            "type", jsn("relation"),
            "quicksearch", jsn(quickSearchUrl.toString()),
            "relation", jsnO(
              //for search
              "direction", jsn("OUT"), //fixme when relationtype_symmetric true then direction BOTH
              "outName", jsn(regularName.get()),
              "inName", jsn(inverseName.get()),
              "targetCollection", jsn(targetType.get()),
              //for CRUD
              "relationCollection", jsn(relationCollectionName),
              "relationTypeId", jsn(timId)
            )
          ));
        } else {
          if (!regularName.isPresent() || !inverseName.isPresent() || !abstractTargetType.isPresent()) {
            LOG.error(
              Logmarkers.databaseInvariant,
              "RelationType should have a relationtype_regularName, relationtype_inverseName and " +
                "relationtype_targetTypeName, but one of those is missing for " + v.id()
            );
          }
        }
      });
    graph.getGraph().traversal().V()
      .not(__.has("relationtype_sourceTypeName", abstractType))
      .has("relationtype_targetTypeName", abstractType)
      .forEachRemaining(v -> {

        String timId = getProp(v, "tim_id", String.class).orElse("<unknown>");
        Optional<String> regularName = getProp(v, "relationtype_regularName", String.class);
        Optional<String> inverseName = getProp(v, "relationtype_inverseName", String.class);
        Optional<String> abstractTargetType = getProp(v, "relationtype_targetTypeName", String.class);
        Optional<String> targetType = abstractTargetType
          .flatMap(typeName -> vre.getImplementerOf(typeName).map(Collection::getCollectionName));

        if (regularName.isPresent() && inverseName.isPresent() && targetType.isPresent()) {
          //special support for keywords:
          URI quickSearchUrl;
          if (abstractTargetType.orElse("").equals("keyword")) {
            quickSearchUrl = Autocomplete.makeUrl(
              targetType.get(),
              Optional.empty(),
              Optional.ofNullable(keywordTypes.get(regularName.get()))
            );
          } else {
            quickSearchUrl = Autocomplete.makeUrl(targetType.get());
          }
          result.add(jsnO(
            "name", jsn(inverseName.get()),
            "type", jsn("relation"),
            "quicksearch", jsn(quickSearchUrl.toString()),
            "relation", jsnO(
              //for search
              "direction", jsn("IN"),
              "outName", jsn(regularName.get()),
              "inName", jsn(inverseName.get()),
              "targetCollection", jsn(targetType.get()),
              //for CRUD
              "relationCollection", jsn(relationCollectionName),
              "relationTypeId", jsn(timId)
            )
          ));
        } else {
          if (!regularName.isPresent() || !inverseName.isPresent() || !abstractTargetType.isPresent()) {
            LOG.error(
              Logmarkers.databaseInvariant,
              "RelationType should have a relationtype_regularName, relationtype_inverseName and " +
                "relationtype_targetTypeName, but one of those is missing for " + v.id()
            );
          }
        }
      });

    return result;
  }

  //copied from TinkerpopJsonCrudService
  public <V> Optional<V> getProp(final Element vertex, final String key, Class<? extends V> clazz) {
    try {
      Iterator<? extends Property<Object>> revProp = vertex.properties(key);
      if (revProp.hasNext()) {
        return Optional.of(clazz.cast(revProp.next().value()));
      } else {
        return Optional.empty();
      }
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }


  public ObjectNode getForVre(String vreName) {
    ObjectNode result = jsnO();
    metadata.getVre(vreName)
      .getCollections()
      .forEach((key, coll) -> result.set(coll.getCollectionName(), getForCollection(coll)));
    return result;
  }
}

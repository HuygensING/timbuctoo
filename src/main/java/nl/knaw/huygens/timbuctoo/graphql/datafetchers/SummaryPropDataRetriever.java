package nl.knaw.huygens.timbuctoo.graphql.datafetchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectGraphReference;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.graphql.defaultconfiguration.DirectionalStep;
import nl.knaw.huygens.timbuctoo.graphql.defaultconfiguration.SummaryProp;
import nl.knaw.huygens.timbuctoo.util.Graph;
import nl.knaw.huygens.timbuctoo.util.RdfConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SummaryPropDataRetriever {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new GuavaModule());
  private static final Logger LOG = LoggerFactory.getLogger(SummaryPropDataRetriever.class);
  private final String summaryPropConfigPredicate;
  private final List<SummaryProp> defaultProperties;

  public SummaryPropDataRetriever(String summaryPropConfigPredicate, List<SummaryProp> defaultProperties) {
    this.defaultProperties = defaultProperties;
    this.summaryPropConfigPredicate = summaryPropConfigPredicate;
  }

  public Optional<TypedValue> createSummaryProperty(SubjectGraphReference source, DataSet dataSet, String typeUri) {
    QuadStore quadStore = dataSet.getQuadStore();

    final Optional<TypedValue> localConfiguredSummaryProp =
      getType(quadStore, source.getSubjectUri(), source.getGraph(), typeUri)
        .flatMap(collection -> getDataQuad(
            quadStore, source.getGraph(), collection.getObject(), summaryPropConfigPredicate))
        .flatMap(userConfigured -> {
          try {
            SummaryProp summaryProp = OBJECT_MAPPER.readValue(userConfigured.getObject(), SummaryProp.class);
            return getDataQuad(summaryProp.getPath(), source.getSubjectUri(), source.getGraph(), quadStore)
              .map(quad -> createTypedValue(quad, dataSet));
          } catch (IOException e) {
            LOG.error("Cannot parse SummaryProp: '{}'", userConfigured.getObject());
          }
          return Optional.empty();
        });

    if (localConfiguredSummaryProp.isPresent()) {
      return localConfiguredSummaryProp;
    } else {
      // fallback to default summary props
      for (SummaryProp prop : defaultProperties) {
        Optional<CursorQuad> quad = getDataQuad(prop.getPath(), source.getSubjectUri(), source.getGraph(), quadStore);
        if (quad.isPresent()) {
          return Optional.of(createTypedValue(quad.get(), dataSet));
        }
      }

      return Optional.empty();
    }
  }

  private Optional<CursorQuad> getType(QuadStore quadStore, String source, Optional<Graph> graph, String typeUri) {
    try (Stream<CursorQuad> possibleCollections = quadStore.getQuadsInGraph(
      source,
      RdfConstants.RDF_TYPE,
      Direction.OUT,
      "",
      graph
    )) {
      return possibleCollections.filter(quad -> quad.getObject().equals(typeUri)).findFirst();
    }
  }

  private Optional<CursorQuad> getDataQuad(QuadStore quadStore, Optional<Graph> graph,
                                           String source, String predicate) {
    try (Stream<CursorQuad> possibleCollections = quadStore.getQuadsInGraph(
      source,
      predicate,
      Direction.OUT,
      "",
      graph
    )) {
      return possibleCollections.findFirst();
    }
  }

  private Optional<CursorQuad> getDataQuad(List<DirectionalStep> path, String uri,
                                           Optional<Graph> graph, QuadStore quadStore) {
    /*
     * A collect of the data might look a bit nicer, but that will cause all the data to be loaded from the database.
     * This way only the data needed is retrieved.
     * See https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#StreamOps for more info.
     */
    Optional<CursorQuad> foundQuad;
    DirectionalStep firstStep = path.getFirst();
    try (Stream<CursorQuad> quads =
             quadStore.getQuadsInGraph(uri, firstStep.getStep(), firstStep.getDirection(), "", graph)) {
      foundQuad = quads.map(quad -> {
        if (path.size() > 1) {
          /*
           * make sure paths are not further retrieved, when the object has a value type
           * this could lead to false positives if the value of the property is a uri used as part of the path
           */
          if (quad.getValuetype().isPresent()) {
            return Optional.<CursorQuad>empty();
          }
          return getDataQuad(path.subList(1, path.size()), quad.getObject(), graph, quadStore);
        } else {
          return Optional.of(quad);
        }
      }).filter(Optional::isPresent).findFirst().map(Optional::get);
    }
    return foundQuad;
  }

  private TypedValue createTypedValue(CursorQuad cursorQuad, DataSet dataSet) {
    /*
     * URI's do not have a value type.
     * So we expect each object without a value type to be a URI.
     * To make a URI a TypedValue we use a "fake" value type for URI's.
     */
    String type = cursorQuad.getValuetype().isPresent() ? cursorQuad.getValuetype().get() : RdfConstants.URI;
    return TypedValue.create(cursorQuad.getObject(), type, dataSet);
  }
}

package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SummaryProp;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
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


  public Optional<TypedValue> createSummaryProperty(SubjectReference source, DataSet dataSet) {
    QuadStore quadStore = dataSet.getQuadStore();

    final Optional<TypedValue> localConfiguredSummaryProp =
      getQuad(quadStore, source.getSubjectUri(), RdfConstants.RDF_TYPE)
        .flatMap(collection -> getQuad(quadStore, collection.getObject(), summaryPropConfigPredicate))
        .flatMap(userConfigured -> {
          try {
            SummaryProp summaryProp = OBJECT_MAPPER.readValue(userConfigured.getObject(), SummaryProp.class);
            return getQuad(summaryProp.getPath(), source.getSubjectUri(), quadStore)
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
        Optional<CursorQuad> quad = getQuad(prop.getPath(), source.getSubjectUri(), quadStore);
        if (quad.isPresent()) {
          return Optional.of(createTypedValue(quad.get(), dataSet));
        }
      }

      return Optional.empty();
    }
  }

  private Optional<CursorQuad> getQuad(QuadStore quadStore, String source, String predicate) {
    try (Stream<CursorQuad> possibleCollections = quadStore.getQuads(
      source,
      predicate,
      Direction.OUT,
      ""
    )) {
      return possibleCollections.findFirst();
    }
  }

  private Optional<CursorQuad> getQuad(List<String> path, String uri, QuadStore quadStore) {
    /*
     * A collect of the data might look a bit nicer, but that will cause all the data to be loaded from the database.
     * This way only the data needed is retrieved.
     * See https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#StreamOps for more info.
     */
    Optional<CursorQuad> foundQuad;
    try (Stream<CursorQuad> quads = quadStore.getQuads(uri, path.get(0), Direction.OUT, "")) {
      foundQuad = quads.map(quad -> {
        if (path.size() > 1) {
          return getQuad(path.subList(1, path.size()), quad.getObject(), quadStore);
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

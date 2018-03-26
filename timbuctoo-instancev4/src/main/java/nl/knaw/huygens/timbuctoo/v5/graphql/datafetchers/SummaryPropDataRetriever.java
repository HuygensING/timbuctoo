package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SummaryProp;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.List;
import java.util.Optional;

public class SummaryPropDataRetriever {
  private final String userConfiguredPredicate;
  private final List<SummaryProp> defaultProperties;

  public SummaryPropDataRetriever(String userConfiguredPredicate, List<SummaryProp> defaultProperties) {
    this.defaultProperties = defaultProperties;
    this.userConfiguredPredicate = userConfiguredPredicate;
  }

  Optional<TypedValue> createSummaryProperty(SubjectReference source, DataSet dataSet) {
    QuadStore quadStore = dataSet.getQuadStore();
    Optional<CursorQuad> userConfigured = quadStore.getQuads(
      source.getSubjectUri(),
      userConfiguredPredicate,
      Direction.OUT,
      ""
    ).findFirst();

    if (userConfigured.isPresent()) {
      return Optional.of(createTypedValue(userConfigured.get(), dataSet));
    } else { // fallback to default summary props
      for (SummaryProp prop : defaultProperties) {
        Optional<CursorQuad> quad = getQuad(prop.getPath(), source.getSubjectUri(), quadStore);
        if (quad.isPresent()) {
          return Optional.of(createTypedValue(quad.get(), dataSet));
        }
      }
    }
    return Optional.empty();
  }

  private Optional<CursorQuad> getQuad(List<String> path, String uri, QuadStore quadStore) {
    /*
     * A collect of the data might look a bit nicer, but that will cause all the data to be loaded from the database.
     * This way only the data needed is retrieved.
     * See https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#StreamOps for more info.
     */
    return quadStore.getQuads(uri, path.get(0), Direction.OUT, "").map(quad -> {
      if (path.size() > 1) {
        return getQuad(path.subList(1, path.size()), quad.getObject(), quadStore);
      } else {
        return Optional.of(quad);
      }
    }).filter(Optional::isPresent).findFirst().map(Optional::get);
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

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
      return Optional.ofNullable(createTypedValue(userConfigured.get(), dataSet));
    } else { // fallback to default summary props
      Optional<CursorQuad> quad = Optional.empty();
      for (SummaryProp prop : defaultProperties) {
        for (String path : prop.getPath()) {
          String uri = quad.isPresent() ? quad.get().getObject() : source.getSubjectUri();
          quad = quadStore.getQuads(uri, path, Direction.OUT, "").findAny();

          if (!quad.isPresent()) {
            break;
          }
        }
        if (quad.isPresent()) {
          break;
        }
      }

      Optional<CursorQuad> foundData = quad;
      if (foundData.isPresent()) {
        return Optional.ofNullable(createTypedValue(foundData.get(), dataSet));
      }
    }
    return Optional.empty();
  }

  private TypedValue createTypedValue(CursorQuad cursorQuad, DataSet dataSet) {
    String type = cursorQuad.getValuetype().isPresent() ? cursorQuad.getValuetype().get() : RdfConstants.STRING;
    return TypedValue.create(cursorQuad.getObject(), type, dataSet);
  }
}

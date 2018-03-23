package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SummaryProp;

import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYDESCRIPTIONPREDICATE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.timPredicate;

public class EntityDescriptionFetcher implements DataFetcher<TypedValue> {
  private final DefaultSummaryPropDataRetriever summaryPropDataRetriever;

  public EntityDescriptionFetcher(List<SummaryProp> defaultDescriptions) {
    summaryPropDataRetriever = new DefaultSummaryPropDataRetriever(defaultDescriptions);
  }

  @Override
  public TypedValue get(DataFetchingEnvironment env) {
    if (env.getSource() instanceof SubjectReference) {
      SubjectReference source = env.getSource();
      DataSet dataSet = source.getDataSet();

      QuadStore quadStore = dataSet.getQuadStore();
      Optional<CursorQuad> desc = quadStore.getQuads(source.getSubjectUri(),
        timPredicate(TIM_SUMMARYDESCRIPTIONPREDICATE), Direction.OUT, "").findFirst();

      if (desc.isPresent()) {
        return createTypedValue(desc.get(), dataSet);
      } else { // fallback to default summary props
        Optional<CursorQuad> foundData = summaryPropDataRetriever.retrieveDefaultProperty(source, quadStore);
        if (foundData.isPresent()) {
          return createTypedValue(foundData.get(), dataSet);
        }
      }
    }
    return null;
  }

  private TypedValue createTypedValue(CursorQuad cursorQuad, DataSet dataSet) {
    String type = cursorQuad.getValuetype().isPresent() ? cursorQuad.getValuetype().get() : STRING;
    return TypedValue.create(cursorQuad.getObject(), type, dataSet);
  }
}

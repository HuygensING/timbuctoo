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
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYTITLEPREDICATE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.timPredicate;

public class EntityTitleFetcher implements DataFetcher<TypedValue> {

  private final DefaultSummaryPropDataRetriever summaryPropDataRetriever;

  public EntityTitleFetcher(List<SummaryProp> defaultTitles) {
    summaryPropDataRetriever = new DefaultSummaryPropDataRetriever(defaultTitles);
  }

  @Override
  public TypedValue get(DataFetchingEnvironment env) {
    if (env.getSource() instanceof SubjectReference) {
      SubjectReference source = env.getSource();
      DataSet dataSet = source.getDataSet();

      QuadStore quadStore = dataSet.getQuadStore();
      Optional<CursorQuad> title = quadStore.getQuads(source.getSubjectUri(),
        timPredicate(TIM_SUMMARYTITLEPREDICATE), Direction.OUT, "").findFirst();

      if (title.isPresent()) {
        return createTypedValue(dataSet, title.get());
      } else { // fallback to default summary props
        Optional<CursorQuad> foundData = summaryPropDataRetriever.retrieveDefaultProperty(source, quadStore);
        if (foundData.isPresent()) {
          return createTypedValue(dataSet, foundData.get());
        }
      }

      // fallback to the uri if no summary props can be found for the title
      return TypedValue.create(source.getSubjectUri(), STRING, dataSet);

    }
    return null;
  }

  private TypedValue createTypedValue(DataSet dataSet, CursorQuad cursorQuad) {
    String type = cursorQuad.getValuetype().isPresent() ? cursorQuad.getValuetype().get() : STRING;
    return TypedValue.create(cursorQuad.getObject(), type, dataSet);
  }
}

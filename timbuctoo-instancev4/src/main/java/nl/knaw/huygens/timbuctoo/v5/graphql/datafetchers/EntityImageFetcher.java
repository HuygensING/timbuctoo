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
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYIMAGEPREDICATE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.timPredicate;

public class EntityImageFetcher implements DataFetcher<TypedValue> {
  private final DefaultSummaryPropDataRetriever summaryPropDataRetriever;

  public EntityImageFetcher(List<SummaryProp> defaultImages) {
    summaryPropDataRetriever = new DefaultSummaryPropDataRetriever(defaultImages);
  }

  @Override
  public TypedValue get(DataFetchingEnvironment env) {
    if (env.getSource() instanceof SubjectReference) {
      SubjectReference source = env.getSource();
      DataSet dataSet = source.getDataSet();

      QuadStore quadStore = dataSet.getQuadStore();
      Optional<CursorQuad> image = quadStore.getQuads(source.getSubjectUri(),
        timPredicate(TIM_SUMMARYIMAGEPREDICATE), Direction.OUT, "").findFirst();

      if (image.isPresent()) {
        return TypedValue.create(image.get().getObject(), STRING, dataSet);
      } else { // fallback to default summary props
        Optional<CursorQuad> foundData = summaryPropDataRetriever.retrieveDefaultProperty(source, quadStore);
        if (foundData.isPresent()) {
          return TypedValue.create(foundData.get().getObject(), STRING, dataSet);
        }
      }

    }
    return null;
  }
}

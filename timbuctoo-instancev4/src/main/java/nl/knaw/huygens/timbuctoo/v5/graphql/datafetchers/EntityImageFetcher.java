package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SummaryProp;

import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYIMAGEPREDICATE;

public class EntityImageFetcher implements DataFetcher<TypedValue> {
  private final SummaryPropDataRetriever summaryPropDataRetriever;

  public EntityImageFetcher(List<SummaryProp> defaultImages) {
    summaryPropDataRetriever = new SummaryPropDataRetriever(TIM_SUMMARYIMAGEPREDICATE, defaultImages);
  }

  @Override
  public TypedValue get(DataFetchingEnvironment env) {
    if (env.getSource() instanceof SubjectReference) {
      SubjectReference source = env.getSource();
      DataSet dataSet = source.getDataSet();
      Optional<TypedValue> summaryProperty = summaryPropDataRetriever.createSummaryProperty(source, dataSet);

      if (summaryProperty.isPresent()) {
        return summaryProperty.get();
      }
    }
    return null;
  }

}

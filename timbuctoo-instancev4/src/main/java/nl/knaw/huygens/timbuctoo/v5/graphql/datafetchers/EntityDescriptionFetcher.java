package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.defaultconfiguration.SummaryProp;

import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SUMMARYDESCRIPTIONPREDICATE;

public class EntityDescriptionFetcher implements DataFetcher<TypedValue> {
  private final SummaryPropDataRetriever summaryPropDataRetriever;

  public EntityDescriptionFetcher(List<SummaryProp> defaultDescriptions) {
    this.summaryPropDataRetriever = new SummaryPropDataRetriever(
      TIM_SUMMARYDESCRIPTIONPREDICATE,
      defaultDescriptions
    );
  }

  @Override
  public TypedValue get(DataFetchingEnvironment env) {
    if (env.getSource() instanceof SubjectReference) {
      SubjectReference source = env.getSource();
      DataSet dataSet = source.getDataSet();

      Optional<TypedValue> desc = summaryPropDataRetriever.createSummaryProperty(source, dataSet);
      if (desc.isPresent()) {
        return desc.get();
      }
    }
    return null;
  }
}

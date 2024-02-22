package nl.knaw.huygens.timbuctoo.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectGraphReference;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.graphql.defaultconfiguration.SummaryProp;
import nl.knaw.huygens.timbuctoo.util.RdfConstants;

import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.graphql.DirectiveRetriever.getDirectiveArgument;

public class EntityImageFetcher implements DataFetcher<TypedValue> {
  private final SummaryPropDataRetriever summaryPropDataRetriever;

  public EntityImageFetcher(List<SummaryProp> defaultImages) {
    this.summaryPropDataRetriever = new SummaryPropDataRetriever(RdfConstants.TIM_SUMMARYIMAGEPREDICATE, defaultImages);
  }

  @Override
  public TypedValue get(DataFetchingEnvironment env) {
    if (env.getSource() instanceof SubjectGraphReference) {
      SubjectGraphReference source = env.getSource();
      DataSet dataSet = source.getDataSet();

      if (env.getParentType() instanceof GraphQLObjectType) {
        String type = getDirectiveArgument((GraphQLObjectType) env.getParentType(), "rdfType", "uri").orElse(null);

        Optional<TypedValue> summaryProperty = summaryPropDataRetriever.createSummaryProperty(source, dataSet, type);
        if (summaryProperty.isPresent()) {
          return summaryProperty.get();
        }
      }
    }
    return null;
  }
}

package nl.knaw.huygens.timbuctoo.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectGraphReference;
import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.graphql.defaultconfiguration.SummaryProp;

import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.graphql.DirectiveRetriever.getDirectiveArgument;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.STRING;

abstract class EntityFetcher implements DataFetcher<TypedValue> {
  private final boolean fallbackToUri;
  private final SummaryPropDataRetriever summaryPropDataRetriever;

  public EntityFetcher(String predicate, List<SummaryProp> defaultProps, boolean fallbackToUri) {
    this.fallbackToUri = fallbackToUri;
    this.summaryPropDataRetriever = new SummaryPropDataRetriever(predicate, defaultProps);
  }

  @Override
  public TypedValue get(DataFetchingEnvironment env) {
    if (env.getSource() instanceof SubjectGraphReference source) {
      DataSet dataSet = source.getDataSet();

      if (env.getParentType() instanceof GraphQLObjectType) {
        String type = getDirectiveArgument((GraphQLObjectType) env.getParentType(), "rdfType", "uri").orElse(null);

        Optional<TypedValue> summaryProperty = summaryPropDataRetriever.createSummaryProperty(source, dataSet, type);
        if (summaryProperty.isPresent()) {
          return summaryProperty.get();
        }
      }

      if (fallbackToUri) {
        // fallback to the uri if no summary props can be found
        return TypedValue.create(source.getSubjectUri(), STRING, dataSet);
      }
    }
    return null;
  }
}

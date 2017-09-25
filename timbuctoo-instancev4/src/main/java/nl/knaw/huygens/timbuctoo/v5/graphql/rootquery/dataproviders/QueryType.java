package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.PromotedDataSet;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class QueryType implements GraphQLQueryResolver {

  private final DataSetRepository dataSetRepository;

  public QueryType(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;

  }

  public List<PromotedDataSet> getPromotedDataSets() {
    return dataSetRepository.getDataSets()
      .values().stream().flatMap(Collection::stream)
      .collect(Collectors.toList());
  }


}

package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
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


  public Optional<User> getAboutMe(DataFetchingEnvironment environment) {
    RootData rootdata = environment.getRoot();

    return rootdata.currentUser;
  }

  public Optional<PromotedDataSet> getDataSetMetadata(String dataSetId) {
    String[] parsedId = dataSetId.split("_", 2);
    return Optional.ofNullable(dataSetRepository.getDataSets().get(parsedId[0])).flatMap(d -> {
      for (PromotedDataSet promotedDataSet : d) {
        if (promotedDataSet.getDataSetId().equals(parsedId[1])) {
          return Optional.of(promotedDataSet);
        }
      }
      return Optional.empty();
    });
  }

  public static class RootData {
    private final Optional<User> currentUser;

    public RootData(Optional<User> currentUser) {
      this.currentUser = currentUser;
    }
  }

}

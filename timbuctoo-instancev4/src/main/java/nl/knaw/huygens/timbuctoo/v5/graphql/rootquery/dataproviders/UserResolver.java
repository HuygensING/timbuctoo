package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders;

import com.coxautodev.graphql.tools.GraphQLResolver;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;

import java.util.Set;

public class UserResolver implements GraphQLResolver<User> {
  private final DataSetRepository dataSetRepository;

  public UserResolver(DataSetRepository dataSetRepository) {
    this.dataSetRepository = dataSetRepository;
  }

  public Set<PromotedDataSet> getDataSets(User input) {
    return dataSetRepository.getDataSets().get(input.getPersistentId());
  }

  public String getId(User input) {
    return input.getPersistentId();
  }

  public String getName(User input) {
    return "Unknown"; //FIXME: implement once we have an actual auth infrastructure
  }

  public String getPersonalInfo(User input) {
    return "http://example.com"; //FIXME: implement once we have an actual auth infrastructure
  }

  public boolean getCanCreateDataSet(User input) {
    return true;
  }

}

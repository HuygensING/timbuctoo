package nl.knaw.huygens.timbuctoo.redirectionservice.bitly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.redirectionservice.RedirectionServiceFactory;
import nl.knaw.huygens.timbuctoo.redirectionservice.RedirectionService;

public class BitlyServiceFactory implements RedirectionServiceFactory {
  @JsonProperty("accessToken")
  private final String accessToken;

  @JsonCreator
  public BitlyServiceFactory(@JsonProperty("accessToken") String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public RedirectionService makeRedirectionService(DataSetRepository dataSetRepository) {
    return new BitlyService(dataSetRepository, accessToken);
  }
}

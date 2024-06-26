package nl.knaw.huygens.timbuctoo.redirectionservice;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface RedirectionServiceFactory {
  /** We use queueing system for redirection services. This is optional for user implementations. */
  RedirectionService makeRedirectionService(DataSetRepository dataSetRepository);
}

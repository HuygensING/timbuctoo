package nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nl.knaw.huygens.timbuctoo.dataset.dto.ImportStatusLabel;

public record ProgressStep(String label, ImportStatusLabel status, String progress, String speed) {
  @JsonIgnore
  static ProgressStep create(String label, ImportStatusLabel status, String progress, String speed) {
    return new ProgressStep(label, status, progress, speed);
  }
}

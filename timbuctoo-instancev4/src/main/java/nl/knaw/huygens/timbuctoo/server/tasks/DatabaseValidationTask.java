package nl.knaw.huygens.timbuctoo.server.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.server.BackgroundRunner;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ValidationResult;

import java.io.PrintWriter;
import java.util.Optional;

public class DatabaseValidationTask extends Task {

  private BackgroundRunner<ValidationResult> validator;

  public DatabaseValidationTask(BackgroundRunner<ValidationResult> validator) {
    super("databasevalidation");
    this.validator = validator;
  }


  @Override
  public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
    final Optional<ValidationResult> mostRecentResult = validator.getMostRecentResult();
    if (mostRecentResult.isPresent()) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      String content = objectMapper.writeValueAsString(mostRecentResult);

      output.write(content);
    } else {
      output.write("No database result");
    }
  }
}

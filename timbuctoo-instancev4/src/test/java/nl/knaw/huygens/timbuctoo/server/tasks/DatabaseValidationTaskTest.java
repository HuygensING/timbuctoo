package nl.knaw.huygens.timbuctoo.server.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.server.BackgroundRunner;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DatabaseValidator;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ValidationResult;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class DatabaseValidationTaskTest {

  @Test
  public void alwaysExecutesWithForceParamPresent() throws Exception {
    BackgroundRunner<ValidationResult> runner = mock(BackgroundRunner.class);
    PrintWriter printWriter = mock(PrintWriter.class);
    DatabaseValidator databaseValidation = mock(DatabaseValidator.class);
    TinkerPopGraphManager graphManager = mock(TinkerPopGraphManager.class);

    ValidationResult validationResult = new ValidationResult() {
      @Override
      public boolean isValid() {
        return true;
      }

      @Override
      public String getMessage() {
        return "foo";
      }
    };

    given(databaseValidation.check(any())).willReturn(validationResult);

    DatabaseValidationTask instance = new DatabaseValidationTask(runner, databaseValidation, graphManager);
    Map<String, String> params = Maps.newHashMap();
    params.put("force", "whatever");

    instance.execute(ImmutableMultimap.copyOf(params.entrySet()), printWriter);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    verify(printWriter, Mockito.atLeastOnce()).write(objectMapper.writeValueAsString(validationResult));
  }

  @Test
  public void doesNotRunWithoutForceParamPresent() throws Exception {
    BackgroundRunner<ValidationResult> runner = mock(BackgroundRunner.class);
    PrintWriter printWriter = mock(PrintWriter.class);
    DatabaseValidator databaseValidation = mock(DatabaseValidator.class);
    TinkerPopGraphManager graphManager = mock(TinkerPopGraphManager.class);
    DatabaseValidationTask instance = new DatabaseValidationTask(runner, databaseValidation, graphManager);
    Map<String, String> params = Maps.newHashMap();

    given(runner.getMostRecentResult()).willReturn(Optional.empty());

    instance.execute(ImmutableMultimap.copyOf(params.entrySet()), printWriter);

    verify(printWriter, Mockito.atLeastOnce()).write("No database result");
  }
}

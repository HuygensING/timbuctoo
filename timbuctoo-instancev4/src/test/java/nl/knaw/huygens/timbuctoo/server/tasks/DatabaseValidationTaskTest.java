package nl.knaw.huygens.timbuctoo.server.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.server.BackgroundRunner;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ValidationResult;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;


public class DatabaseValidationTaskTest {

    @Test
    public void alwaysExecutesWithForceParamPresent() throws Exception {
        ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);
        BackgroundRunner<ValidationResult> runner = new BackgroundRunner<>(0, Clock.systemDefaultZone(), executor);
        PrintWriter printWriter = Mockito.mock(PrintWriter.class);

        DatabaseValidationTask instance = new DatabaseValidationTask(runner);
        Map<String, String> params = Maps.newHashMap();
        params.put("force", "whatever");

        instance.execute(ImmutableMultimap.copyOf(params.entrySet()), printWriter);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        Mockito.verify(printWriter, Mockito.atLeastOnce()).write(objectMapper.writeValueAsString(Optional.empty()));
    }

    @Test
    public void doesNotRunWithoutForceParamPresent() throws Exception {
        ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);
        BackgroundRunner<ValidationResult> runner = new BackgroundRunner<>(0, Clock.systemDefaultZone(), executor);
        PrintWriter printWriter = Mockito.mock(PrintWriter.class);
        DatabaseValidationTask instance = new DatabaseValidationTask(runner);

        instance.execute(ImmutableMultimap.copyOf(Maps.<String, String>newHashMap().entrySet()), printWriter);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        Mockito.verify(printWriter, Mockito.atLeastOnce()).write("No database result");
    }
}
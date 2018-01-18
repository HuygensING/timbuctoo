package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.TimeWithUnit;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.List;

@Value.Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME)
public interface ImportStatusReport {

  @Nullable
  String getMethodName();

  @Nullable
  String getBaseUri();

  @Nullable
  String getStatus();

  @Nullable
  String getDate();

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  List<String> getMessages();

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  List<String> getErrors();

  int getErrorCount();

  // included in Java bean for convenience, not serialized.
  boolean hasErrors();

  @JsonIgnore
  @Nullable
  Throwable getLastError();

  TimeWithUnit getElapsedTime();
}

package nl.knaw.huygens.timbuctoo.v5.permissions.satisfiable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.function.Function;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type")
public interface Satisfiable extends Function<String, Boolean> {
}

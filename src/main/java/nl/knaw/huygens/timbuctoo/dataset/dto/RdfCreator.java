package nl.knaw.huygens.timbuctoo.dataset.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface RdfCreator {
}

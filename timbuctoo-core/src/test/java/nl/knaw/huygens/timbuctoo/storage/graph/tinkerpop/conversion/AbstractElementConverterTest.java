package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.Element;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.hamcrest.ListContainsItemsInAnyOrderMatcher.containsInAnyOrder;
import static nl.knaw.huygens.timbuctoo.config.TypeNames.getInternalName;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class AbstractElementConverterTest {
  protected void verifyTypeIsSet(Element elementMock, Class<? extends Entity>... types) throws Exception {
    List<String> internalNames = Arrays.stream(types).map(type -> getInternalName(type)).collect(toList());


    ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
    verify(elementMock).setProperty(argThat(is(ELEMENT_TYPES)), valueCaptor.capture());

    List<String> value = new ObjectMapper().readValue(valueCaptor.getValue(), new TypeReference<List<String>>() {
    });

    assertThat(value, containsInAnyOrder(internalNames));
  }

  protected String getTypesAsString(List<String> typeNames) throws JsonProcessingException {

    ObjectMapper objectMapper = new ObjectMapper();
    String value = objectMapper.writeValueAsString(typeNames);

    return value;
  }

  protected PropertyConverter createPropertyConverter(String propertyName, String fieldName, FieldType fieldType) {
    PropertyConverter propertyConverter = mock(PropertyConverter.class);
    when(propertyConverter.completePropertyName()).thenReturn(propertyName);
    when(propertyConverter.getFieldName()).thenReturn(fieldName);
    when(propertyConverter.getFieldType()).thenReturn(fieldType);
    return propertyConverter;
  }

}

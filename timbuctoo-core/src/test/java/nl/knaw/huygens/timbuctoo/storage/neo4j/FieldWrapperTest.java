package nl.knaw.huygens.timbuctoo.storage.neo4j;

import org.junit.Test;

public interface FieldWrapperTest {
  @Test
  void addValueToNodeSetsThePropertyWithTheFieldNameToTheValueOfTheNode() throws Exception;

  @Test
  void addValueToNodeDoesNotSetIfTheValueIsNull() throws Exception;
}

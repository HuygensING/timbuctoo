package nl.knaw.huygens.timbuctoo.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RelationTypeTest {

  @Test
  public void testConstructor() {
    RelationType relationType = new RelationType("name", DomainEntity.class);
    assertEquals("name", relationType.getRegularName());
    assertEquals("name", relationType.getInverseName());
    assertEquals(DomainEntity.class, relationType.getSourceDocType());
    assertEquals(DomainEntity.class, relationType.getTargetDocType());
  }

}

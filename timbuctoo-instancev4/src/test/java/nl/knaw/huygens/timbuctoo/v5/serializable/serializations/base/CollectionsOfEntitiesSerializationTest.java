package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import org.junit.Test;

public class CollectionsOfEntitiesSerializationTest {

  @Test
  public void performSerialization() throws Exception {
    // CollectionsOfEntitiesSerialization sut = new TestCollectionsOfEntitiesSerialization();
    //
    // sut.serialize(SourceData.simpleResult());
    //
    // assertThat(sut.allEntities.size(), is(3));
    // assertThat(sut.allEntities.get("http://example.com/OtherSubItem").size(), is(1));
    // assertThat(sut.allEntities.get("http://example.com/Person").size(), is(3));
    // assertThat(sut.allEntities.get("http://example.com/SubItem").size(), is(3));
    //
    // Map<String, CollectionsOfEntitiesSerialization.AggregatedEntity> persons = sut
    //   .allEntities.get("http://example.com/Person");
    // List person1BList = persons.get("http://example.com/1").relations.get("b");
    // List person2BList = persons.get("http://example.com/2").relations.get("b");
    // assertThat(person1BList.get(0), sameInstance(person2BList.get(0)));
    // assertThat(person1BList.get(1), sameInstance(person2BList.get(1)));
  }


  class TestCollectionsOfEntitiesSerialization extends CollectionsOfEntitiesSerialization {
  }
}

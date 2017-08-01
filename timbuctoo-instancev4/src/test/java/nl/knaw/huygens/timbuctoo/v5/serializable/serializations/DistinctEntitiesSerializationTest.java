package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;

public class DistinctEntitiesSerializationTest {

  @Test
  public void performSerialization() throws Exception {
    DistinctEntitiesSerialization sut = new TestDistinctEntitiesSerialization();

    sut.serialize(SourceData.simpleResult());

    assertThat(sut.allEntities.size(), is(3));
    assertThat(sut.allEntities.get("http://example.com/OtherSubItem").size(), is(1));
    assertThat(sut.allEntities.get("http://example.com/Person").size(), is(3));
    assertThat(sut.allEntities.get("http://example.com/SubItem").size(), is(3));

    Map<String, Map<String, Object>> persons = sut.allEntities.get("http://example.com/Person");
    List person1BList = (List) persons.get("http://example.com/1").get("b");
    List person2BList = (List) persons.get("http://example.com/2").get("b");
    assertThat(person1BList.get(0), sameInstance(person2BList.get(0)));
    assertThat(person1BList.get(1), sameInstance(person2BList.get(1)));
  }


  class TestDistinctEntitiesSerialization extends DistinctEntitiesSerialization {
  }
}

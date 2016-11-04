package nl.knaw.huygens.timbuctoo.database.converters.json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntityImpl;
import nl.knaw.huygens.timbuctoo.database.dto.RelationRef;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.dto.property.StringProperty;
import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.security.UserStore;
import org.junit.Test;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

// TODO before refactoring this class improve coverage
public class EntityToJsonMapperTest {

  @Test
  public void mapEntityMapsTheTypeAndId() throws Exception {
    ReadEntityImpl readEntity = new ReadEntityImpl();
    UUID id = UUID.randomUUID();
    readEntity.setId(id);
    String userId = "userId";
    Change change = new Change(Instant.now().toEpochMilli(), userId, null);
    readEntity.setCreated(change);
    readEntity.setModified(change);
    String type = "otherType";
    readEntity.setTypes(Lists.newArrayList("type", type));
    readEntity.setDeleted(false);
    readEntity.setRev(1);
    readEntity.setPid("pid");
    readEntity.setProperties(Lists.newArrayList());
    UserStore userStore = mock(UserStore.class);
    when(userStore.userForId(userId)).thenReturn(Optional.of(new User("User Name")));
    EntityToJsonMapper instance = new EntityToJsonMapper(
      userStore,
      (collection, id1, rev) -> URI.create("www.example.com")
    );
    Collection collection = mock(Collection.class);
    when(collection.getEntityTypeName()).thenReturn(type);

    ObjectNode resutlJson = instance.mapEntity(
      collection,
      readEntity,
      false,
      (readEntity1, resultJson) -> {
      },
      (relationRef, resultJson) -> {
      }
    );


    assertThat(
      resutlJson.toString(),
      sameJSONAs(jsnO("_id", jsn(id.toString()), "@type", jsn(type)).toString()).allowingExtraUnexpectedFields()
    );
  }

  @Test
  public void mapEntityMapsTheProperties() throws Exception {
    ReadEntityImpl readEntity = new ReadEntityImpl();
    readEntity.setId(UUID.randomUUID());
    String userId = "userId";
    Change change = new Change(Instant.now().toEpochMilli(), userId, null);
    readEntity.setCreated(change);
    readEntity.setModified(change);
    String type = "otherType";
    readEntity.setTypes(Lists.newArrayList("type", type));
    readEntity.setDeleted(false);
    readEntity.setRev(1);
    readEntity.setPid("pid");
    ArrayList<TimProperty<?>> properties = Lists.newArrayList();
    properties.add(new StringProperty("name", "Name"));
    readEntity.setProperties(properties);
    UserStore userStore = mock(UserStore.class);
    String userName = "User Name";
    when(userStore.userForId(userId)).thenReturn(Optional.of(new User(userName)));
    EntityToJsonMapper instance = new EntityToJsonMapper(
      userStore,
      (collection, id1, rev) -> URI.create("www.example.com")
    );
    Collection collection = mock(Collection.class);
    when(collection.getEntityTypeName()).thenReturn(type);

    ObjectNode resutlJson = instance.mapEntity(
      collection,
      readEntity,
      false,
      (readEntity1, resultJson) -> {
      },
      (relationRef, resultJson) -> {
      }
    );


    assertThat(
      resutlJson.toString(),
      sameJSONAs(jsnO("name", jsn("Name")).toString()).allowingExtraUnexpectedFields()
    );
  }

  @Test
  public void mapEntityMapsTheRelations() throws Exception {
    ReadEntityImpl readEntity = new ReadEntityImpl();
    UUID id = UUID.randomUUID();
    readEntity.setId(id);
    String userId = "userId";
    Change change = new Change(Instant.now().toEpochMilli(), userId, null);
    readEntity.setCreated(change);
    readEntity.setModified(change);
    String type = "otherType";
    readEntity.setTypes(Lists.newArrayList("type", type));
    readEntity.setDeleted(false);
    readEntity.setRev(1);
    readEntity.setPid("pid");
    readEntity.setProperties(Lists.newArrayList());
    String otherEntity = UUID.randomUUID().toString();
    String relType = "relType";
    readEntity.setRelations(Lists.newArrayList(new RelationRef(otherEntity, "otherColl", "otherType", true, "relId", 1,
      relType, "displayName")));
    UserStore userStore = mock(UserStore.class);
    String userName = "User Name";
    when(userStore.userForId(userId)).thenReturn(Optional.of(new User(userName)));
    EntityToJsonMapper instance = new EntityToJsonMapper(
      userStore,
      (collection, id1, rev) -> URI.create("www.example.com")
    );
    Collection collection = mock(Collection.class);
    when(collection.getEntityTypeName()).thenReturn(type);

    ObjectNode resutlJson = instance.mapEntity(
      collection,
      readEntity,
      true,
      (readEntity1, resultJson) -> {
      },
      (relationRef, resultJson) -> {
      }
    );

    assertThat(resutlJson.toString(), sameJSONAs(jsnO(
      "@relationCount", jsn(1),
      "@relations", jsnO(
        relType, jsnA(
          jsnO(
            "id", jsn(otherEntity)
          )
        )
      )
    ).toString()).allowingExtraUnexpectedFields());
  }

}

package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.model.properties.converters.StringToStringConverter;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class EntityTest {
  private static final String type = new StringToStringConverter().getUniqueTypeIdentifier();

  @Test
  public void addPropertyAddsThePropertyToTheEntityForEachCollectionTheEntityIsConnectedTo() {
    Vertex vertex = mock(Vertex.class);
    Collection collection1 = mock(Collection.class);
    Collection collection2 = mock(Collection.class);
    Entity entity = new Entity(vertex, Sets.newHashSet(collection1, collection2));
    String propName = "propName";
    String value = "value";

    entity.addProperty(propName, value, new StringToStringConverter().getUniqueTypeIdentifier());

    verify(collection1).addProperty(vertex, propName, value, type);
    verify(collection2).addProperty(vertex, propName, value, type);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void addToCollectionAddsTheEntityToTheCollection() {
    Vertex vertex = mock(Vertex.class);
    Collection newCollection = mock(Collection.class);
    given(newCollection.getArchetype()).willReturn(Optional.empty());
    Collection otherCollection = mock(Collection.class);
    Set<Collection> collections = Sets.newHashSet(otherCollection);
    TypesHelper typesHelper = mock(TypesHelper.class);
    Entity instance = new Entity(vertex, collections, typesHelper, mock(PropertyHelper.class));

    instance.addToCollection(newCollection);

    verify(newCollection).add(argThat(is(vertex)));
    assertThat(collections, containsInAnyOrder(newCollection, otherCollection));
    ArgumentCaptor<Set> collectionsCaptor = ArgumentCaptor.forClass(Set.class);
    verify(typesHelper).addTypeInformation(
      argThat(is(vertex)),
      (Set<Collection>) collectionsCaptor.capture(),
      argThat(is(newCollection))
    );
    assertThat((Set<Collection>) collectionsCaptor.getValue(),
      containsInAnyOrder(otherCollection, newCollection));
  }

  @Test
  public void moveToCollectionAddsTheCurrentPropertiesOfTheEntityToTheNewCollection() {
    Vertex vertex = mock(Vertex.class);
    Collection oldCollection = mock(Collection.class);
    when(oldCollection.getArchetype()).thenReturn(Optional.empty());

    Collection newCollection = mock(Collection.class);
    Collection archetypeCollection = mock(Collection.class);
    when(newCollection.getArchetype()).thenReturn(Optional.of(archetypeCollection));
    Set<Collection> collections = Sets.newHashSet(oldCollection);
    PropertyHelper propertyHelper = mock(PropertyHelper.class);
    TypesHelper typesHelper = mock(TypesHelper.class);
    Entity instance = new Entity(vertex, collections, typesHelper, propertyHelper);

    instance.moveToOtherArchetype(oldCollection, newCollection);

    verify(propertyHelper).movePropertiesToNewCollection(instance, oldCollection, newCollection);
  }

  @Test
  public void removeFromCollectionRemovesTheVertexOfTheEntityFromTheCollection() {
    Vertex vertex = mock(Vertex.class);
    Collection collectionToRemoveFrom = mock(Collection.class);
    given(collectionToRemoveFrom.getArchetype()).willReturn(Optional.empty());
    Collection otherCollection = mock(Collection.class);
    Set<Collection> collections = Sets.newHashSet(collectionToRemoveFrom, otherCollection);
    TypesHelper typesHelper = mock(TypesHelper.class);
    PropertyHelper propertyHelper = mock(PropertyHelper.class);
    Entity entity = new Entity(vertex, collections, typesHelper, propertyHelper);

    entity.removeFromCollection(collectionToRemoveFrom);

    verify(collectionToRemoveFrom).remove(vertex);
    assertThat(collections, contains(otherCollection));
  }

  //FIXME: once we're using easily constructable objects for collections and vertices I need to add tests to verify
  //that adding and removing from a collection also adds and removes it from the archetype (in case of remove a remove
  //should only remove from the archetyp if no other collection inherits from that archetype)

  @SuppressWarnings("unchecked")
  @Test
  public void removeFromCollectionRemovesUpdatesTheTypesInformation() {
    Vertex vertex = mock(Vertex.class);
    Collection collectionToRemoveFrom = mock(Collection.class);
    given(collectionToRemoveFrom.getArchetype()).willReturn(Optional.empty());
    Collection otherCollection = mock(Collection.class);
    Set<Collection> collections = Sets.newHashSet(collectionToRemoveFrom, otherCollection);
    TypesHelper typesHelper = mock(TypesHelper.class);
    PropertyHelper propertyHelper = mock(PropertyHelper.class);
    Entity entity = new Entity(vertex, collections, typesHelper, propertyHelper);

    entity.removeFromCollection(collectionToRemoveFrom);

    ArgumentCaptor<Set> collectionCaptor = ArgumentCaptor.forClass(Set.class);
    verify(typesHelper).removeTypeInformation(
      argThat(is(vertex)),
      collectionCaptor.capture(),
      argThat(is(collectionToRemoveFrom))
    );
    assertThat((Set<Collection>) collectionCaptor.getValue(), contains(otherCollection));
  }
}

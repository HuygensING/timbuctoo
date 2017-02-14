package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ArchetypeTripleProcessorTest {

  private static final String VRE_NAME = "vreName";
  private static final String ENTITY_TYPE_NAME = "subject";
  private static final String SUBJECT_URI = "http://example.com/" + ENTITY_TYPE_NAME;
  private static final String PREDICATE_URI = "http://example.com/predicate";
  private static final String OBJECT_NAME = "object";
  private static final String OBJECT_URI = "http://example.com/" + OBJECT_NAME;
  private Collection collection;
  private Collection archetypeCollection;
  private ArchetypeTripleProcessor instance;
  private Database database;
  private Entity entity1;
  private Entity entity2;
  private Collection previousArchetype;
  private Collection defaultArchetype;

  @Before
  public void setup() {
    collection = mock(Collection.class);
    previousArchetype = mock(Collection.class);
    given(collection.getArchetype()).willReturn(Optional.of(previousArchetype));
    archetypeCollection = mock(Collection.class);
    database = mock(Database.class);
    given(database.findOrCreateCollection(VRE_NAME, SUBJECT_URI, ENTITY_TYPE_NAME)).willReturn(collection);
    given(database.findArchetypeCollection(OBJECT_NAME)).willReturn(Optional.of(archetypeCollection));
    entity1 = mock(Entity.class);
    entity2 = mock(Entity.class);
    Set<Entity> entitiesOfCollection = Sets.newHashSet(entity1, entity2);
    given(database.findEntitiesByCollection(collection)).willReturn(entitiesOfCollection);
    defaultArchetype = mock(Collection.class);
    given(database.getConcepts()).willReturn(defaultArchetype);

    instance = new ArchetypeTripleProcessor(database);
  }

  @Test
  public void processSetsTheCollectionArchetype() {
    instance.process(VRE_NAME, SUBJECT_URI, PREDICATE_URI, OBJECT_URI, true);

    verify(collection).setArchetype(archetypeCollection, OBJECT_URI);
  }

  @Test
  public void processMovesTheEntitiesOfTheCollectionToTheNewArchetype() {
    instance.process(VRE_NAME, SUBJECT_URI,PREDICATE_URI,OBJECT_URI,true);

    verify(entity1).moveToOtherArchetype(previousArchetype, archetypeCollection);
    verify(entity2).moveToOtherArchetype(previousArchetype, archetypeCollection);
  }

  @Test
  public void processDoesNotChangeTheArchetypeWhenTheNewArchetypeDoesNotExist() {
    given(database.findArchetypeCollection(OBJECT_NAME)).willReturn(Optional.empty());

    instance.process(VRE_NAME,SUBJECT_URI,PREDICATE_URI,OBJECT_URI,true);

    verify(collection, never()).setArchetype(any(Collection.class), anyString());
    verifyZeroInteractions(entity1, entity2);
  }

  @Test
  public void processMovesTheCollectionToTheDefaultArchetypeForARetraction() {
    instance.process(VRE_NAME,SUBJECT_URI,PREDICATE_URI,OBJECT_URI,false);

    verify(collection).setArchetype(defaultArchetype, "");
  }

  @Test
  public void processMovesTheEntitiesOfTheCollectionToTheDefaultArchetypeForARetraction() {
    instance.process(VRE_NAME,SUBJECT_URI,PREDICATE_URI,OBJECT_URI,false);

    verify(entity1).moveToOtherArchetype(previousArchetype, defaultArchetype);
    verify(entity2).moveToOtherArchetype(previousArchetype, defaultArchetype);
  }

  @Test
  public void processDoesNotRemoveTheArchetypeWhenTheArchetypeOfTheTripleDoesNotExist() {
    given(database.findArchetypeCollection(OBJECT_NAME)).willReturn(Optional.empty());

    instance.process(VRE_NAME,SUBJECT_URI,PREDICATE_URI,OBJECT_URI,false);

    verify(collection, never()).setArchetype(any(Collection.class), anyString());
    verifyZeroInteractions(entity1, entity2);
  }

}

package nl.knaw.huygens.timbuctoo.storage;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;

import java.util.Date;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import test.model.BaseDomainEntity;
import test.model.DomainEntityWithMiscTypes;
import test.model.DomainEntityWithReferences;
import test.model.TestSystemEntity;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Tests the EntityInducer and EntityReducer in the simplest possible way:
 * start with an entity, induce, reduce, giving an entity that should match
 * the original entity.
 */
public class EntityInducerReducerTest {

  private final static String ID = "TEST042";

  private static TypeRegistry registry;

  private EntityInducer inducer;
  private EntityReducer reducer;

  @BeforeClass
  public static void setupRegistry() {
    registry = new TypeRegistry("test.model test.model.projecta test.model.projectb");
  }

  @Before
  public void setup() throws Exception {
    inducer = new EntityInducer();
    reducer = new EntityReducer(registry);
  }

  private void validateEntityProperties(Entity initial, Entity reduced) {
    assertEquals(initial.getId(), reduced.getId());
    assertEquals(initial.getRev(), reduced.getRev());
    assertEquals(initial.getCreated(), reduced.getCreated());
    assertEquals(initial.getModified(), reduced.getModified());
  }

  private void validateSystemEntityProperties(SystemEntity initial, SystemEntity reduced) {
    validateEntityProperties(initial, reduced);
  }

  private void validateDomainEntityProperties(DomainEntity initial, DomainEntity reduced) {
    validateEntityProperties(initial, reduced);
    assertEquals(initial.getPid(), reduced.getPid());
    assertEquals(initial.getVariations(), reduced.getVariations());
  }

  private void validateBaseDomainEntityProperties(BaseDomainEntity initial, BaseDomainEntity reduced) {
    validateDomainEntityProperties(initial, reduced);
    assertEquals(initial.getValue1(), reduced.getValue1());
    assertEquals(initial.getValue2(), reduced.getValue2());
  }

  // -------------------------------------------------------------------

  @Test
  public void testSystemEntity() throws Exception {
    TestSystemEntity initial = new TestSystemEntity(ID, "v1", "v2", null);

    JsonNode tree = inducer.induceSystemEntity(TestSystemEntity.class, initial);
    TestSystemEntity reduced = reducer.reduceVariation(TestSystemEntity.class, tree);

    validateSystemEntityProperties(initial, reduced);
    assertEquals(initial.getValue1(), reduced.getValue1());
    assertEquals(initial.getValue2(), reduced.getValue2());
    assertEquals(initial.getValue3(), reduced.getValue3());
  }

  @Test
  public void testDomainEntityWithComplexProperty() throws Exception {
    DomainEntityWithReferences initial = new DomainEntityWithReferences(ID);
    initial.setSharedReference(new Reference("type1", "id1"));
    initial.setUniqueReference(new Reference("type2", "id2"));

    JsonNode tree = inducer.induceDomainEntity(DomainEntityWithReferences.class, initial);
    DomainEntityWithReferences reduced = reducer.reduceVariation(DomainEntityWithReferences.class, tree);

    validateBaseDomainEntityProperties(initial, reduced);
    assertEquals(initial.getSharedReference(), reduced.getSharedReference());
    assertEquals(initial.getUniqueReference(), reduced.getUniqueReference());
  }

  @Test
  public void testDomainEntityWithMiscTypes() throws Exception {
    DomainEntityWithMiscTypes initial = new DomainEntityWithMiscTypes(ID);
    initial.setDate(new Date());
    initial.setType(String.class);
    PersonName name = new PersonName();
    name.addNameComponent(Type.FORENAME, "test");
    name.addNameComponent(Type.SURNAME, "test");
    initial.setPersonName(name);

    JsonNode tree = inducer.induceDomainEntity(DomainEntityWithMiscTypes.class, initial);
    DomainEntityWithMiscTypes reduced = reducer.reduceVariation(DomainEntityWithMiscTypes.class, tree);

    validateBaseDomainEntityProperties(initial, reduced);
    assertEquals(initial.getDate(), reduced.getDate());
    assertEquals(initial.getType(), reduced.getType());
    assertEquals(initial.getPersonName(), reduced.getPersonName());
  }

}

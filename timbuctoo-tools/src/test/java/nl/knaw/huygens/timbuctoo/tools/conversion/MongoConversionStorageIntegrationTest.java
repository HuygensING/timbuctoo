package nl.knaw.huygens.timbuctoo.tools.conversion;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Person.Gender;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.storage.EntityInducer;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;
import nl.knaw.huygens.timbuctoo.storage.Properties;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.mongo.EntityIds;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoDB;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoProperties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.model.projecta.ProjectAPerson;
import test.model.projectb.ProjectBPerson;

import com.google.common.collect.Lists;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class MongoConversionStorageIntegrationTest {

  private static final Change CHANGE_TO_SAVE = Change.newInternalInstance();
  //DomainEntity constants
  private static final Class<ProjectAPerson> DOMAIN_ENTITY_TYPE = ProjectAPerson.class;
  private static final Datable BIRTH_DATE = new Datable("1800");
  private static final Datable BIRTH_DATE1 = new Datable("10001213");
  private static final Datable DEATH_DATE = new Datable("19000101");
  private static final Datable DEATH_DATE1 = new Datable("11000201");
  private static final Gender GENDER = Gender.MALE;
  private static final Gender GENDER1 = Gender.FEMALE;
  private static final PersonName PERSON_NAME = PersonName.newInstance("Constantijn", "Huygens");
  private static final PersonName PERSON_NAME1 = PersonName.newInstance("Maria", "Reigersberch");
  private static final String PID = "pid";
  private static final String PID2 = "pid2";
  private static final String PROJECT_A_PERSON_PROPERTY = "projectAPersonProperty";

  private static final int DB_PORT = 12345;
  private static final MongodStarter starter = MongodStarter.getDefaultInstance();
  private MongodExecutable mongodExe;
  private MongodProcess mongod;
  private MongoClient mongo;
  private MongoDB mongoDB;
  private MongoConversionStorage instance;

  @Before
  public void setup() throws ModelException, UnknownHostException, IOException {
    TypeRegistry typeRegistry = TypeRegistry.getInstance();
    typeRegistry.init("nl.knaw.huygens.timbuctoo.model.* test.model.projecta test.model.projectb");

    Properties properties = new MongoProperties();
    EntityInducer inducer = new EntityInducer(properties);
    EntityReducer reducer = new EntityReducer(properties, typeRegistry);

    mongodExe = starter.prepare(new MongodConfigBuilder().version(Version.Main.PRODUCTION)//
        .net(new Net(DB_PORT, Network.localhostIsIPv6()))//
        .build());

    mongod = mongodExe.start();
    mongo = new MongoClient("localhost", DB_PORT);

    mongoDB = new MongoDB(mongo, mongo.getDB("test"));

    instance = new MongoConversionStorage(mongoDB, new EntityIds(typeRegistry, mongoDB), properties, inducer, reducer);
  }

  @After
  public void cleanup() {
    mongod.stop();
    mongodExe.stop();
  }

  @Test
  public void getAllVersionVariationsReturnsAnIteratorThatReturnsAllVariantsInTheOrderOfTheRevisions() throws Exception {
    // setup
    String id = addDefaultProjectAPerson();
    instance.setPID(DOMAIN_ENTITY_TYPE, id, PID);

    // add a new variant
    ProjectAPerson projectAPerson = instance.getEntity(DOMAIN_ENTITY_TYPE, id);

    ProjectBPerson projectBPerson = createProjectBPerson(GENDER1, PERSON_NAME1, BIRTH_DATE1, DEATH_DATE1);
    projectBPerson.setId(projectAPerson.getId());
    projectBPerson.setRev(projectAPerson.getRev());

    instance.updateDomainEntity(ProjectBPerson.class, projectBPerson, CHANGE_TO_SAVE);
    instance.setPID(ProjectBPerson.class, id, PID2);

    // action
    AllVersionVariationMap<Person> allVersionVariations = instance.getAllVersionVariationsMapOf(Person.class, id);

    // verify
    List<Integer> keysInOrder = allVersionVariations.revisionsInOrder();
    assertThat(keysInOrder, contains(1, 2));

    List<Person> firstRevVariations = allVersionVariations.get(1);
    assertThat(firstRevVariations, hasSize(2));
    assertThat(firstRevVariations.get(0).getRev(), is(1));

    List<Person> secondRevVariations = allVersionVariations.get(2);
    assertThat(secondRevVariations, hasSize(3));
    assertThat(secondRevVariations.get(0).getRev(), is(2));
  }

  @Test
  public void getAllVersionVariationsReturnsAnIteratorThatReturnsOneRevisionIfTheTypeIsNotPersisted() throws StorageException {
    // setup
    String id = addDefaultProjectAPerson();

    // action
    AllVersionVariationMap<Person> map = instance.getAllVersionVariationsMapOf(Person.class, id);

    // verify
    assertThat(map.revisionsInOrder(), contains(1));
    List<Person> firstRevVariations = map.get(1);
    assertThat(firstRevVariations, hasSize(2));
    assertThat(firstRevVariations.get(0).getRev(), is(1));
  }

  private String addDefaultProjectAPerson() throws StorageException {
    return addPerson(GENDER, PERSON_NAME, PROJECT_A_PERSON_PROPERTY, BIRTH_DATE, DEATH_DATE);
  }

  private String addPerson(Gender gender, PersonName name, String projectAPersonProperty, Datable birthDate, Datable deathDate) throws StorageException {
    ProjectAPerson domainEntityToStore = createProjectAPerson(gender, name, projectAPersonProperty, birthDate, deathDate);
    return instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntityToStore, CHANGE_TO_SAVE);
  }

  private ProjectAPerson createProjectAPerson(Gender gender, PersonName name, String projectAPersonProperty, Datable birthDate, Datable deathDate) {
    ProjectAPerson person = new ProjectAPerson();
    person.setGender(gender);
    person.addName(name);
    person.setProjectAPersonProperty(projectAPersonProperty);
    person.setBirthDate(birthDate);
    person.setDeathDate(deathDate);
    person.setTypes(Lists.newArrayList("Test"));

    return person;
  }

  private ProjectBPerson createProjectBPerson(Gender gender, PersonName name, Datable birthDate, Datable deathDate) {
    ProjectBPerson person = new ProjectBPerson();
    person.setGender(gender);
    person.addName(name);
    person.setBirthDate(birthDate);
    person.setDeathDate(deathDate);
    person.setTypes(Lists.newArrayList("Test"));

    return person;
  }

}

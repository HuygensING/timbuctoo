package nl.knaw.huygens.repository.importer.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.util.Configuration;

public class PersonImporter {

  public static void main(String[] args) throws SQLException, ConfigurationException, IOException {
    Configuration conf = new Configuration("config.xml");
    Hub hub = new Hub();
    StorageManager storageManager = new StorageManager(conf, hub);
    storageManager.getStorage().empty();
    String mySQLURL = "jdbc:mysql://localhost:3306/raa_web";
    String mySQLUser = "root";
    String mySQLPwd = "M4SQ1!";
    String query = "SELECT voornaam, tussenvoegsel, geslachtsnaam, geboortedatum, overlijdensdatum FROM persoon;";

    Map<String, List<String>> mySQLPropertyMapping = new HashMap<String, List<String>>();
    mySQLPropertyMapping.put("name", Arrays.asList(new String[] { "voornaam", "tussenvoegsel", "geslachtsnaam" }));
    mySQLPropertyMapping.put("birthDate", Arrays.asList(new String[] { "geboortedatum" }));
    mySQLPropertyMapping.put("deathDate", Arrays.asList(new String[] { "overlijdensdatum" }));

    importPersons(mySQLURL, mySQLUser, mySQLPwd, query, mySQLPropertyMapping, storageManager);

    String postGreSQLURL = "jdbc:postgresql://localhost:5432/bia";
    String postGreSQLUser = "bia_user";
    String postGreSQLPwd = "B14_Us3r";
    String postGreSQLuery = "SELECT family_name, given_name, preposition, intraposition, postposition, birth_date, death_date FROM persons;";

    Map<String, List<String>> postGreSQLPropertyMapping = new HashMap<String, List<String>>();
    postGreSQLPropertyMapping.put("name", Arrays.asList(new String[] { "given_name", "preposition", "intraposition", "postposition", "family_name" }));
    postGreSQLPropertyMapping.put("birthDate", Arrays.asList(new String[] { "birth_date" }));
    postGreSQLPropertyMapping.put("deathDate", Arrays.asList(new String[] { "death_date" }));

    importPersons(postGreSQLURL, postGreSQLUser, postGreSQLPwd, postGreSQLuery, postGreSQLPropertyMapping, storageManager);

  }

  private static void importPersons(String mySQLURL, String mySQLUser, String mySQLPwd, String query, Map<String, List<String>> propertyMapping, StorageManager storageManager)
      throws SQLException, IOException {
    PersonResultSetConverter mySQLConverter = new PersonResultSetConverter(propertyMapping);

    SQLImporter mySQLImporter = new SQLImporter(mySQLURL, mySQLUser, mySQLPwd);
    List<Person> persons = mySQLImporter.executeQuery(query, mySQLConverter);

    for (Person person : persons) {
      System.out.println(person.getId() + ": " + person.getDescription());
      storageManager.addDocument(person, Person.class);
    }

    System.out.println("persons.size(): " + persons.size());
  }
}

package nl.knaw.huygens.timbuctoo.tools.importer.ckcc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.ckcc.CKCCPerson;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCPerson;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.importer.CSVImporter;

/**
 * Imports CKCC person data from a CSV file.
 * This importer is used to enrich the DWC-BIA data.
 */
public class CKCCPersonImporter extends CSVImporter {

  private final Change change;
  private StorageManager storageManager;

  public CKCCPersonImporter(StorageManager storageManager) {
    super(new PrintWriter(System.err));
    change = new Change("importer", "ckcc");
    this.storageManager = storageManager;
    System.out.println("%n=== Importing documents of type 'CKCCPerson'");
  }

  @Override
  protected void handleLine(String[] items) {
    try {
      boolean exists = false;
      String id = items[8];
      Person retrieved = null;
      if (id.length() > 0 && !id.equals("?")) {
        retrieved = storageManager.getEntity(Person.class, id);
        exists = retrieved != null;
      }
      CKCCPerson person = new CKCCPerson();
      person.setName(buildName(items));
      person.setGender(items[5]);
      person.setBirthDate(new Datable(items[6]));
      person.setDeathDate(new Datable(items[7]));
      if (exists) {
        person.setId(retrieved.getId());
        person.setPid(retrieved.getPid());
        storageManager.updateDomainEntity(CKCCPerson.class, person, change);
      } else {
        storageManager.addDomainEntity(CKCCPerson.class, person, change);
      }
    } catch (IOException e) {
      displayError(e.getMessage(), items);
    }
  }

  private PersonName buildName(String[] items) {
    PersonName name = new PersonName();
    name.addNameComponent(Type.ROLE_NAME, items[0]);
    name.addNameComponent(Type.FORENAME, items[1]);
    name.addNameComponent(Type.NAME_LINK, items[2]);
    name.addNameComponent(Type.SURNAME, items[3]);
    name.addNameComponent(Type.ADD_NAME, items[4]);
    return name;
  }

  @Override
  protected void handleEndOfFile() {
    StorageIterator<CKCCPerson> iterator = storageManager.getAll(CKCCPerson.class);
    while (iterator.hasNext()) {
      Person person = iterator.next();
      String id = person.getId();
      System.out.println(id);
      List<Person> persons = storageManager.getAllVariations(Person.class, id);
      for (Person p : persons) {
        Class<? extends Person> cls = p.getClass();
        if (cls != Person.class && cls != DWCPerson.class) {
          System.out.printf("%-20s %-40s %-12s %-12s%n", cls.getSimpleName(), p.getName(), p.getBirthDate(), p.getDeathDate());
        }
      }
    }
  }

}

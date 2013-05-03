package nl.knaw.huygens.repository.importer.database;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.ckcc.CKCCPerson;
import nl.knaw.huygens.repository.model.dwcbia.DWCScientist;
import nl.knaw.huygens.repository.model.util.Datable;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.util.CSVImporter;

/**
 * Imports CKCC person data from a CSV file.
 */
public class CKCCPersonImporter extends CSVImporter {

  private StorageManager storageManager;

  public CKCCPersonImporter(StorageManager storageManager) {
    super(new PrintWriter(System.err));
    this.storageManager = storageManager;
    System.out.println("=== Importing documents of type 'CKCCPerson'");
  }

  @Override
  protected void handleLine(String[] items) {
    try {
      boolean exists = false;
      String id = items[8];
      Person retrieved = null;
      if (id.length() > 0 && !id.equals("?")) {
        retrieved = storageManager.getCompleteDocument(Person.class, id);
        exists = retrieved != null;
      }
      CKCCPerson person = new CKCCPerson();
      person.name = buildName(items);
      person.setGender(items[5]);
      person.birthDate = new Datable(items[6]);
      person.deathDate = new Datable(items[7]);
      if (exists) {
        person.setId(retrieved.getId());
        person.setPid(retrieved.getPid());
        storageManager.modifyDocument(CKCCPerson.class, person);
      } else {
        storageManager.addDocument(CKCCPerson.class, person);
      }
    } catch (IOException e) {
      displayError(e.getMessage(), items);
    }
  }

  private String buildName(String[] items) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 5; i++) {
      if (items[i].length() > 0) {
        if (builder.length() > 0) {
          builder.append(' ');
        }
        builder.append(items[i]);
      }
    }
    return builder.toString();
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
        if (p.getClass() != Person.class && p.getClass() != DWCScientist.class) {
          System.out.printf("%-20s %-40s %-12s %-12s%n", p.getTypeName(), p.getName(), p.getBirthDate(), p.getDeathDate());
        }
      }
    }
  }

}

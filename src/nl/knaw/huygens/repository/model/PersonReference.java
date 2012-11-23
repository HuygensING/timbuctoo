package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.model.storage.GenericDBRef;

public class PersonReference {
  public String role = "";
  public String pages = "";
  public boolean certain;
  public String remarks = "";
  public GenericDBRef<Person> person;
}
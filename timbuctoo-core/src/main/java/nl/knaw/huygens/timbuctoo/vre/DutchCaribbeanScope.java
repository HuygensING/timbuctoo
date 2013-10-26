package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.Archive;
import nl.knaw.huygens.timbuctoo.model.Archiver;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Keyword;
import nl.knaw.huygens.timbuctoo.model.Legislation;
import nl.knaw.huygens.timbuctoo.model.Person;

public class DutchCaribbeanScope extends AbstractScope {

  private static final String DCAR_PACKAGE = "timbuctoo.model.dcar";

  public DutchCaribbeanScope() throws IOException {
    // primitive entity types
    addClass(Archive.class);
    addClass(Archiver.class);
    addClass(Keyword.class);
    addClass(Legislation.class);
    addClass(Person.class);
    fixBaseTypes();
    // additional entity types
    addPackage(DCAR_PACKAGE);
    fixAllTypes();
  }

  @Override
  public String getName() {
    return "DutchCaribbeanScope";
  }

  @Override
  public <T extends DomainEntity> boolean inScope(Class<T> type, String id) {
    return true;
  }

}

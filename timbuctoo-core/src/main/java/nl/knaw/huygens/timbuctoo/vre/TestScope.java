package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARArchive;

public class TestScope extends AbstractScope {

  public TestScope() throws IOException {
    super("timbuctoo.model.dcar");
  }

  @Override
  public String getId() {
    return "test";
  }

  @Override
  public String getName() {
    return "Test Scope";
  }

  @Override
  public <T extends DomainEntity> boolean inScope(Class<T> type, String id) {
    return true;
  }

  @Override
  public <T extends DomainEntity> boolean inScope(T entity) {
    Class<? extends DomainEntity> entityType = entity.getClass();
    if (entityType == DCARArchive.class) {
      String text = ((DCARArchive) entity).getTitleEng();
      return (text != null) && text.contains("Cura");
    }
    return false;
  }

}

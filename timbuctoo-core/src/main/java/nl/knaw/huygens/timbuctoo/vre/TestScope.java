package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARArchive;

public class TestScope extends AbstractScope {

  private static final String DCAR_PACKAGE = "timbuctoo.model.dcar";

  public TestScope() throws IOException {
    super(DCAR_PACKAGE);
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

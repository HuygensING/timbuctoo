package nl.knaw.huygens.timbuctoo.vre;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARArchive;

public class TestScope extends AbstractScope {

  public TestScope() throws IOException {
    addClass(DCARArchive.class);
    buildTypes();
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
    return super.inScope(type, id);
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

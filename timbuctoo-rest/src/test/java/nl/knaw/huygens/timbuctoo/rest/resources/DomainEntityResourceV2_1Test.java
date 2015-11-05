package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
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

import nl.knaw.huygens.timbuctoo.config.Paths;
import test.rest.model.projecta.ProjectARelation;

import static nl.knaw.huygens.timbuctoo.config.TypeNames.getExternalName;

public class DomainEntityResourceV2_1Test extends DomainEntityResourceV2Test {
  private static final Class<ProjectARelation> RELATION_TYPE = ProjectARelation.class;
  private static final String RELATION_RESOURCE = getExternalName(RELATION_TYPE);

  @Override
  protected String getAPIVersion() {
    return Paths.V2_1_PATH;
  }

}

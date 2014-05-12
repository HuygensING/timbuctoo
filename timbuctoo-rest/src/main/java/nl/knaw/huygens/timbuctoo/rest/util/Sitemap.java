package nl.knaw.huygens.timbuctoo.rest.util;

/*
 * #%L
 * Timbuctoo REST api
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

import java.util.List;

import javax.ws.rs.core.Application;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.rest.resources.DomainEntityResource;
import nl.knaw.huygens.timbuctoo.rest.util.JAXUtils.API;

import com.google.common.collect.Lists;

public class Sitemap {

  public final String description = "Repository Sitemap";
  public final List<API> availableAPIList;

  private static final String ENTITY_REGEXP = "\\{" + DomainEntityResource.ENTITY_PARAM + "\\}";

  public Sitemap(Application application, TypeRegistry registry) {
    availableAPIList = Lists.newArrayList();
    for (Class<?> cls : application.getClasses()) {
      List<API> apis = JAXUtils.generateAPIs(cls);
      if (cls == DomainEntityResource.class) {
        for (Class<? extends DomainEntity> type : registry.getDomainEntityTypes()) {
          String name = TypeNames.getExternalName(type);
          for (API api : apis) {
            availableAPIList.add(api.modifyPath(ENTITY_REGEXP, name));
          }
        }
      } else {
        availableAPIList.addAll(apis);
      }
    }
  }

}

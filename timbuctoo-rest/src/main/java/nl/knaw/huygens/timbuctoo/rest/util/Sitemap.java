package nl.knaw.huygens.timbuctoo.rest.util;

import java.util.List;

import javax.ws.rs.core.Application;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
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
        for (String type : registry.getTypeStrings()) {
          for (API api : apis) {
            availableAPIList.add(api.modifyPath(ENTITY_REGEXP, type));
          }
        }
      } else {
        availableAPIList.addAll(apis);
      }
    }
  }

}

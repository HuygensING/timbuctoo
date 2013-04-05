package nl.knaw.huygens.repository.model;

import java.util.List;

import javax.ws.rs.core.Application;

import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.resources.RESTAutoResource;
import nl.knaw.huygens.repository.util.JAXUtils;
import nl.knaw.huygens.repository.util.JAXUtils.API;

import com.google.common.collect.Lists;

public class Sitemap {

  public final String description = "Repository Sitemap";
  public final List<API> availableAPIList;

  public Sitemap(Application application, DocumentTypeRegister registry) {
    availableAPIList = Lists.newArrayList();
    for (Class<?> cls : application.getClasses()) {
      List<API> apis = JAXUtils.generateAPIs(cls);
      if (cls == RESTAutoResource.class) {
        for (String type : registry.getTypeStrings()) {
          for (API api : apis) {
            availableAPIList.add(api.modifyPath(RESTAutoResource.ENTITY_PARAM, type));
          }
        }
      } else {
        availableAPIList.addAll(apis);
      }
    }
  }

}

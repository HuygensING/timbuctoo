package nl.knaw.huygens.repository.model;

import java.util.List;

import javax.ws.rs.core.Application;

import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.model.util.IDPrefix;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.util.JAXUtils;
import nl.knaw.huygens.repository.util.JAXUtils.API;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

@IDPrefix("STM")
public class Sitemap extends Document {

  public List<API> availableAPIList;
  private String defaultVRE;

  public Sitemap(Application app, DocumentTypeRegister registry) {
    setId(String.format("T%d", System.currentTimeMillis()));

    availableAPIList = Lists.newArrayList();
    for (Class<?> cls : JAXUtils.getAllResources(app)) {
      availableAPIList.addAll(JAXUtils.generateAPIs(cls));
    }
  }

  @Override
  public String getDescription() {
    return "Repository Sitemap";
  }

  @Override
  public void fetchAll(Storage storage) {
    // No-op;
  }

  @Override
  @JsonProperty("!defaultVRE")
  public String getDefaultVRE() {
    return defaultVRE;
  }

  @Override
  @JsonProperty("!defaultVRE")
  public void setDefaultVRE(String defaultVRE) {
    this.defaultVRE = defaultVRE;
  }

}

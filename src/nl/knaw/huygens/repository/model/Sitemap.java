package nl.knaw.huygens.repository.model;

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import nl.knaw.huygens.repository.model.util.IDPrefix;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.util.JAXUtils;
import nl.knaw.huygens.repository.util.JAXUtils.API;

@IDPrefix("STM")
public class Sitemap extends Document {
  public List<API> availableAPIList;
  private String defaultVRE;
  
  public Sitemap(Application app) {
    setId(String.format("T%d", System.currentTimeMillis()));
    List<API> rv = Lists.newArrayList();
    Set<Class<?>> allResources = JAXUtils.getAllResources(app);
    for (Class<?> cls : allResources) {
      List<API> generatedAPIs = JAXUtils.generateAPIs(cls);
      if (generatedAPIs != null) {
        rv.addAll(generatedAPIs);
      }
    }
    availableAPIList = rv;
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
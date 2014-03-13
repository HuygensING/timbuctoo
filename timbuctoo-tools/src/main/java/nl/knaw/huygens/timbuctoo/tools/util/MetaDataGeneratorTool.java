package nl.knaw.huygens.timbuctoo.tools.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class MetaDataGeneratorTool {
  private static Logger LOG = LoggerFactory.getLogger(MetaDataGeneratorTool.class);
  private MetaDataGenerator generator;
  public String saveDir;

  public static void main(String[] args) {
    if (args == null || args.length == 0) {
      LOG.error("Give a directory as first argument.");
      return;
    }

    new MetaDataGeneratorTool(args[0]).execute();
  }

  public MetaDataGeneratorTool(String saveDir) {
    this.saveDir = saveDir;
    generator = new MetaDataGenerator(new FieldMapper());
  }

  public void execute() {
    ClassPath classPath = null;
    try {
      classPath = ClassPath.from(this.getClass().getClassLoader());
    } catch (IOException e) {
      LOG.error("Could not load classpath", e);
      return;
    }

    for (ClassInfo info : classPath.getTopLevelClassesRecursive("nl.knaw.huygens.timbuctoo.model")) {
      String name = info.getName();
      try {
        Class<?> type = Class.forName(name);

        createMetaData(type);

        // create metadata for the inner classes aswell.
        for (Class<?> declaredType : type.getDeclaredClasses()) {
          createMetaData(declaredType);
        }

      } catch (ClassNotFoundException e) {
        LOG.info("Could not find class {}", name);
      }
    }
  }

  private void createMetaData(Class<?> type) {
    LOG.info("Generating metaData for type: {}", type.getSimpleName());

    try {
      Map<String, String> metaDataMap = generator.generate(type);
      //LOG.info(new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(metaDataMap));
      save(metaDataMap, type);
    } catch (JsonProcessingException e) {
      LOG.error("Mapping object went wrong.", e);
    } catch (IOException e) {
      LOG.error("Saving the data went wrong.", e);
    }
  }

  private void save(Map<String, String> metaDataMap, Class<?> type) throws JsonGenerationException, JsonMappingException, IOException {
    File file = new File(this.saveDir, getNormalizedName(type) + ".json");
    System.out.println("file: " + file.getAbsolutePath());

    // toArray is needed to make use of the TimbuctooTypeIdResolver
    new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(file, metaDataMap);
  }

  protected String getNormalizedName(Class<?> type) {
    return type.getSimpleName().toLowerCase();
  }

}

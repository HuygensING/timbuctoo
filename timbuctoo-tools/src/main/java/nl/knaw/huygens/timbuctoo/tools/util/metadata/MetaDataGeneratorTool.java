package nl.knaw.huygens.timbuctoo.tools.util.metadata;

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

  public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
    if (args == null || args.length == 0) {
      LOG.error("Pass a directory as first argument.");
      return;
    }

    new MetaDataGeneratorTool(args[0]).execute();
  }

  public MetaDataGeneratorTool(String saveDir) {
    this.saveDir = saveDir;
    generator = new MetaDataGenerator(new FieldMapper());
  }

  public void execute() throws IllegalArgumentException, IllegalAccessException {
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

        createMetaData(type, null);

        // create metadata for the inner classes aswell.
        for (Class<?> declaredType : type.getDeclaredClasses()) {
          createMetaData(declaredType, type);
        }

      } catch (ClassNotFoundException e) {
        LOG.info("Could not find class {}", name);
      }
    }
  }

  private void createMetaData(Class<?> type, Class<?> containingType) throws IllegalArgumentException, IllegalAccessException {
    if (!type.isEnum()) {
      LOG.info("Generating metaData for type: {}", type.getSimpleName());

      try {
        Map<String, Object> metaDataMap = generator.generate(type);
        //LOG.info(new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(metaDataMap));
        save(metaDataMap, type, containingType);
      } catch (JsonProcessingException e) {
        LOG.error("Mapping object went wrong.", e);
      } catch (IOException e) {
        LOG.error("Saving the data went wrong.", e);
      }
    }
  }

  private void save(Map<String, Object> metaDataMap, Class<?> type, Class<?> containingType) throws JsonGenerationException, JsonMappingException, IOException {
    File file = new File(this.saveDir, getNormalizedName(type, containingType) + ".json");
    System.out.println("file: " + file.getAbsolutePath());

    // toArray is needed to make use of the TimbuctooTypeIdResolver
    new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(file, metaDataMap);
  }

  protected String getNormalizedName(Class<?> type, Class<?> containingType) {
    StringBuilder sb = new StringBuilder();
    if (containingType != null) {
      sb.append(containingType.getSimpleName().toLowerCase());
      sb.append(".");
    }

    sb.append(type.getSimpleName().toLowerCase());

    return sb.toString();
  }

}

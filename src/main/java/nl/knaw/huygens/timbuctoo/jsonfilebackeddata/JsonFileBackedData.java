package nl.knaw.huygens.timbuctoo.jsonfilebackeddata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nl.knaw.huygens.timbuctoo.jacksonserializers.TimbuctooCustomSerializers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class JsonFileBackedData<T> implements JsonDataStore<T> {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .registerModule(new Jdk8Module())
    .registerModule(new JavaTimeModule())
    .registerModule(new GuavaModule())
    .registerModule(new TimbuctooCustomSerializers())
    .enable(SerializationFeature.INDENT_OUTPUT);

  private static final Map<String, JsonFileBackedData> EXISTING = new HashMap<>();

  //We make sure that there's only one instance per file so that we don't have multiple simultaneous writes
  public static <T> JsonFileBackedData<T> getOrCreate(File file, Supplier<T> emptyValue, TypeReference<T> valueType)
      throws IOException {
    return getOrCreate(file, emptyValue, valueType, null);
  }

  //We make sure that there's only one instance per file so that we don't have multiple simultaneous writes
  public static <T> JsonFileBackedData<T> getOrCreate(File file, Supplier<T> emptyValue, TypeReference<T> valueType,
                                                      Function<T, T> hydrator) throws IOException {
    synchronized (EXISTING) {
      if (EXISTING.containsKey(file.getCanonicalPath())) {
        return EXISTING.get(file.getCanonicalPath());
      } else {
        return new JsonFileBackedData<>(file, emptyValue, valueType, hydrator);
      }
    }
  }

  public static void remove(File file) throws IOException {
    synchronized (EXISTING) {
      EXISTING.remove(file.getCanonicalPath());
    }
  }

  private final File file;
  private T value;

  private JsonFileBackedData(File file, Supplier<T> emptyValue, TypeReference<T> valueType, Function<T,T> hydrator)
      throws IOException {
    this.file = file;
    if (file.exists()) {
      value = OBJECT_MAPPER.readValue(file, valueType);
      if (hydrator != null) {
        value = hydrator.apply(value);
      }
    } else {
      file.getParentFile().mkdirs(); // make sure the directories exist
      value = emptyValue.get();
      OBJECT_MAPPER.writeValue(file, value);
    }
    //is synchronized because ctor is called from a synchronized block
    EXISTING.put(file.getCanonicalPath(), this);
  }

  @Override
  public void updateData(Function<T, T> mutator) throws IOException {
    synchronized (file) {
      value = mutator.apply(value);
      OBJECT_MAPPER.writeValue(file, value);
    }
  }

  @Override
  public T getData() {
    return value;
  }

  static void regenerateSoWeCanTestHowWellLoadingWorks(File file) throws IOException {
    synchronized (EXISTING) {
      EXISTING.remove(file.getCanonicalPath());
    }
  }
}

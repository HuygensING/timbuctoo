package nl.knaw.huygens.timbuctoo.v5.jsonfilebackeddata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class JsonFileBackedDataTest {
  @Test
  public void itStoresData() throws Exception {
    File tmpFile = Files.createTempFile("prefix", "asd").toFile();
    try {
      tmpFile.delete(); //the file must either not exist, or contain valid data
      JsonFileBackedData<Map<String, String>> instance = JsonFileBackedData.getOrCreate(
        tmpFile,
              HashMap::new,
              new TypeReference<>() {
              }
      );

      assertThat(instance.getData().isEmpty(), is(true));
      assertThat(Files.readString(tmpFile.toPath()), is("{ }"));

      instance.updateData(data -> {
        data.put("foo", "utf-8 ☃");
        return data;
      });

      assertThat(Files.readString(tmpFile.toPath()), is("{\n  \"foo\" : \"utf-8 ☃\"\n}"));

      JsonFileBackedData.regenerateSoWeCanTestHowWellLoadingWorks(tmpFile);

      JsonFileBackedData<Map<String, String>> reloadedInstance = JsonFileBackedData.getOrCreate(
        tmpFile,
              HashMap::new,
              new TypeReference<>() {
              }
      );

      assertThat(reloadedInstance.getData().get("foo"), is("utf-8 ☃"));
    } finally {
      tmpFile.delete();
    }
  }

  @Test
  public void itCanBeInitializedToNull() throws Exception {
    File tmpFile = Files.createTempFile("prefix", "asd").toFile();
    try {
      tmpFile.delete(); //the file must either not exist, or contain valid data
      JsonFileBackedData<Map<String, String>> instance = JsonFileBackedData.getOrCreate(
        tmpFile,
        () -> null,
              new TypeReference<>() {
              }
      );
      assertThat(instance.getData(), is(nullValue()));
      assertThat(Files.readString(tmpFile.toPath()), is("null"));
    } finally {
      tmpFile.delete(); //the file must either not exist, or contain valid data
    }
  }


}

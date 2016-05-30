package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.Change;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseLog {

  private final ObjectMapper objectMapper;
  private final BufferedWriter writer;

  public DatabaseLog() {
    objectMapper = new ObjectMapper();
    try {
      writer = Files.newBufferedWriter(Paths.get("dblog"), Charset.forName("UTF-8"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void newVertex(Vertex vertex) {
    String modifiedString = vertex.value("modified");

    try {
      Change modified = objectMapper.readValue(modifiedString, Change.class);
      writeAndFlush(
        String.format("%d - Vertex with tim_id '%s' created%n", modified.getTimeStamp(), vertex.value("tim_id")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void updateVertex(Vertex vertex) {
    String modifiedString = vertex.value("modified");

    try {
      Change modified = objectMapper.readValue(modifiedString, Change.class);
      writeAndFlush(
        String.format("%d - Vertex with tim_id '%s' updated%n", modified.getTimeStamp(), vertex.value("tim_id")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void newProperty(VertexProperty property) {
    try {
      writeAndFlush(String.format("Property '%s' set to '%s'%n", property.key(), property.value()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void updateProperty(VertexProperty property) {
    try {
      writeAndFlush(String.format("Property '%s' set to '%s'%n", property.key(), property.value()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteProperty(String propertyName) {

    try {
      writeAndFlush(String.format("Property '%s' removed%n", propertyName));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeAndFlush(String format) throws IOException {
    writer.write(format);
    writer.flush();
  }
}

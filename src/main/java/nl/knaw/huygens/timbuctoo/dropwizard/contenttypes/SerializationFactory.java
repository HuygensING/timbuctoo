package nl.knaw.huygens.timbuctoo.dropwizard.contenttypes;

import nl.knaw.huygens.timbuctoo.serializable.Serialization;

import java.io.IOException;
import java.io.OutputStream;

public interface SerializationFactory {
  Serialization create(OutputStream outputStream) throws IOException;
}

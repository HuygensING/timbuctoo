package nl.knaw.huygens.timbuctoo.v5.datastores.filestore;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

public interface FileStore {
  URI storeFile(InputStream data, Optional<MediaType> mimetype);
}

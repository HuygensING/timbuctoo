package nl.knaw.huygens.timbuctoo.v5.datastores.imagestore;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

public interface ImageStore {
  URI storeImage(InputStream data, Optional<MediaType> mimetype);
}

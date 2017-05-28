package nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import javax.ws.rs.core.MediaType;
import java.nio.charset.Charset;
import java.util.Optional;

@Value.Immutable
public interface FileInfo {
  @JsonCreator
  static FileInfo create(@JsonProperty("name") String name, @JsonProperty("mediatype") String mediatype,
                         @JsonProperty("charset") String charset) {
    return ImmutableFileInfo.builder()
      .name(name)
      .mediaType(Optional.ofNullable(mediatype).map(MediaType::valueOf))
      .charset(Optional.ofNullable(charset).map(Charset::forName))
      .build();
  }

  static FileInfo create(String name, Optional<MediaType> mediatype, Optional<Charset> charset) {
    return ImmutableFileInfo.builder()
      .name(name)
      .mediaType(mediatype)
      .charset(charset)
      .build();
  }

  String getName();

  Optional<MediaType> getMediaType();

  Optional<Charset> getCharset();
}

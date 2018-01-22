package nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import javax.ws.rs.core.MediaType;
import java.nio.charset.Charset;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableFileInfo.class)
@JsonDeserialize(as = ImmutableFileInfo.class)
public interface FileInfo {
  static FileInfo create(String name, String dateTime, String mediatype, String charset) {
    return ImmutableFileInfo.builder()
      .name(name)
      .dateTime(dateTime)
      .mediaType(MediaType.valueOf(mediatype))
      .charset(Optional.ofNullable(charset).map(Charset::forName))
      .build();
  }

  static FileInfo create(String name, String dateTime, MediaType mediatype, Optional<Charset> charset) {
    return ImmutableFileInfo.builder()
      .name(name)
      .dateTime(dateTime)
      .mediaType(mediatype)
      .charset(charset)
      .build();
  }

  String getName();

  MediaType getMediaType();

  Optional<Charset> getCharset();

  String getDateTime();
}

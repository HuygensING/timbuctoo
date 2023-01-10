package nl.knaw.huygens.timbuctoo.filestorage.implementations.filesystem.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.knaw.huygens.timbuctoo.filestorage.implementations.filesystem.dto.ImmutableFileInfo;
import org.immutables.value.Value;

import javax.ws.rs.core.MediaType;
import java.nio.charset.Charset;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableFileInfo.class)
@JsonDeserialize(as = ImmutableFileInfo.class)
public interface FileInfo {
  static FileInfo create(String name, String mediatype, String charset, boolean replaceData, boolean isInverse) {
    return ImmutableFileInfo.builder()
                            .name(name)
                            .mediaType(MediaType.valueOf(mediatype))
                            .charset(Optional.ofNullable(charset).map(Charset::forName))
                            .replaceData(replaceData)
                            .isInverse(isInverse)
                            .build();
  }

  static FileInfo create(String name, MediaType mediatype, Optional<Charset> charset,
                         boolean replaceData, boolean isInverse) {
    return ImmutableFileInfo.builder()
                            .name(name)
                            .mediaType(mediatype)
                            .charset(charset)
                            .replaceData(replaceData)
                            .isInverse(isInverse)
                            .build();
  }

  String getName();

  MediaType getMediaType();

  Optional<Charset> getCharset();

  @Value.Default
  default boolean getReplaceData() {
    return false;
  }

  @Value.Default
  default boolean getIsInverse() {
    return false;
  }
}

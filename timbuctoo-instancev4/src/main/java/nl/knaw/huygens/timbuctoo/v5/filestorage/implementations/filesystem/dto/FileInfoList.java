package nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public interface FileInfoList {
  @JsonCreator
  static FileInfoList create(@JsonProperty("items") Map<String, FileInfo> items) {
    return ImmutableFileInfoList.builder().items(items).build();
  }

  static FileInfoList create() {
    return ImmutableFileInfoList.builder().build();
  }

  default FileInfoList addItem(String token, FileInfo item) {
    return ImmutableFileInfoList.builder().from(this).putItems(token, item).build();
  }

  Map<String, FileInfo> getItems();
}

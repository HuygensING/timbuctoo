package nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.v5.filestorage.LogStorage;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedFile;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.dto.FileInfo;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.dto.FileInfoList;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.dto.FileSystemCachedFile;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.dto.FileSystemCachedLog;
import nl.knaw.huygens.timbuctoo.v5.jsonfilebackeddata.JsonFileBackedData;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class FileSystemFileStorage implements FileStorage, LogStorage {
  private final File dir;
  private final JsonFileBackedData<FileInfoList> fileInfo;

  public FileSystemFileStorage(File dir) throws IOException {
    this.dir = dir;
    dir.mkdirs();
    fileInfo = JsonFileBackedData.getOrCreate(
      new File(dir, "fileList.json"),
      FileInfoList::create,
      new TypeReference<FileInfoList>() {}
    );
  }

  @Override
  public String saveFile(InputStream stream, String fileName, MediaType mediaType) throws IOException {
    return storeFile(stream, fileName, mediaType, Optional.empty());
  }

  private String storeFile(InputStream stream, String fileName, MediaType mediaType,
                           Optional<Charset> charset) throws IOException {
    String date = Instant.now().toString();
    String random = UUID.randomUUID().toString();
    String mnemonic = fileName.replaceAll("[^a-zA-Z0-9]", "_");
    String token = date.replaceAll("[:\\.TZ]", "-") + random + "-" + mnemonic;

    try {
      // Gives (Too many open files) on Mac after ~1000 calls. commons.IOUtils.copy is no better.
      Files.copy(stream, new File(dir, token).toPath());
      fileInfo.updateData(data -> data.addItem(token, FileInfo.create(fileName, date, mediaType, charset)));
    } finally {
      stream.close();
    }

    return token;
  }

  @Override
  public CachedFile getFile(String token) throws IOException {
    FileInfo fileInfo = this.fileInfo.getData().getItems().get(token);
    return new FileSystemCachedFile(fileInfo.getMediaType(), fileInfo.getName(), new File(dir, token));
  }

  @Override
  public String saveLog(InputStream stream, String fileName, MediaType mediaType, Optional<Charset> charset)
    throws IOException {
    return storeFile(stream, fileName, mediaType, charset);
  }

  @Override
  public CachedLog getLog(String token) throws IOException {
    FileInfo fileInfo = this.fileInfo.getData().getItems().get(token);
    return new FileSystemCachedLog(
      fileInfo.getMediaType(),
      fileInfo.getCharset(),
      fileInfo.getName(),
      new File(dir, token)
    );
  }
}

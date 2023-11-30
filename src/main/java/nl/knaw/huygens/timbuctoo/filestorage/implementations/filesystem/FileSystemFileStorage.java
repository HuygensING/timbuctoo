package nl.knaw.huygens.timbuctoo.filestorage.implementations.filesystem;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.knaw.huygens.timbuctoo.filestorage.implementations.filesystem.dto.FileInfo;
import nl.knaw.huygens.timbuctoo.filestorage.implementations.filesystem.dto.FileInfoList;
import nl.knaw.huygens.timbuctoo.filestorage.implementations.filesystem.dto.FileSystemCachedFile;
import nl.knaw.huygens.timbuctoo.filestorage.implementations.filesystem.dto.FileSystemCachedLog;
import nl.knaw.huygens.timbuctoo.jsonfilebackeddata.JsonFileBackedData;
import nl.knaw.huygens.timbuctoo.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.filestorage.LogStorage;
import nl.knaw.huygens.timbuctoo.filestorage.dto.CachedFile;
import nl.knaw.huygens.timbuctoo.filestorage.dto.CachedLog;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileSystemFileStorage implements FileStorage, LogStorage {
  private final File dir;
  private final JsonFileBackedData<FileInfoList> fileInfo;
  private final File fileList;

  public FileSystemFileStorage(File dir) throws IOException {
    this.dir = dir;
    dir.mkdirs();
    fileList = new File(dir, "fileList.json");
    fileInfo = JsonFileBackedData.getOrCreate(
      fileList,
      FileInfoList::create,
      new TypeReference<>() {}
    );
  }

  @Override
  public String saveFile(InputStream stream, String fileName, MediaType mediaType) throws IOException {
    return storeFile(stream, fileName, mediaType, Optional.empty());
  }

  private String storeFile(InputStream stream, String fileName, MediaType mediaType,
                           Optional<Charset> charset) throws IOException {
    String random = UUID.randomUUID().toString();
    String mnemonic = fileName.replaceAll("[^a-zA-Z0-9]", "_");
    String token = random + "-" + mnemonic;

    if (charset.isPresent()) {
      stream = new PushbackInputStream(stream, 2);
    }

    OutputStream out = new FileOutputStream(new File(dir, token));
    if (charset.isPresent() && !isGzipCompressed((PushbackInputStream) stream)) {
      out = new GZIPOutputStream(out);
    }

    try {
      stream.transferTo(out);
    } finally {
      out.close();
    }

    fileInfo.updateData(data -> data.addItem(token, FileInfo.create(fileName, mediaType, charset)));

    return token;
  }

  @Override
  public Optional<CachedFile> getFile(String token) throws IOException {
    CachedFile cachedFile = null;
    FileInfo fileInfo = this.fileInfo.getData().getItems().get(token);
    if (fileInfo != null) {
      cachedFile = new FileSystemCachedFile(fileInfo.getMediaType(), fileInfo.getName(), new File(dir, token));
    }
    return Optional.ofNullable(cachedFile);
  }

  @Override
  public void clear() throws IOException {
    JsonFileBackedData.remove(fileList);
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

  private static boolean isGzipCompressed(PushbackInputStream pb) throws IOException {
    int header = pb.read();
    if (header == -1) {
      return false;
    }

    int byteRead = pb.read();
    if (byteRead == -1) {
      pb.unread(header);
      return false;
    }

    pb.unread(new byte[]{(byte) header, (byte) byteRead});
    header = (byteRead << 8) | header;

    return header == GZIPInputStream.GZIP_MAGIC;
  }
}

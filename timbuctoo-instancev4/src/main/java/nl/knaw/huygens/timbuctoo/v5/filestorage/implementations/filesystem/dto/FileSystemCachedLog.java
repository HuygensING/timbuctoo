package nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.dto;

import com.google.common.base.Charsets;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;
import org.mozilla.universalchardet.UniversalDetector;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Optional;

public class FileSystemCachedLog implements CachedLog {

  private final Optional<MediaType> mimeType;
  private final Optional<Charset> charset;
  private final String name;
  private final File file;

  public FileSystemCachedLog(Optional<MediaType> mimeType, Optional<Charset> charset, String name, File file) {
    this.mimeType = mimeType;
    this.charset = charset;
    this.name = name;
    this.file = file;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public File getFile() {
    return file;
  }

  @Override
  public Reader getReader() throws IOException {
    if (charset.isPresent()) {
      return new InputStreamReader(new FileInputStream(file), charset.get());
    } else {
      byte[] buf = new byte[4096];
      final UniversalDetector detector;
      try (FileInputStream stream = new FileInputStream(file)) {
        detector = new UniversalDetector(null);

        int readCount;
        while ((readCount = stream.read(buf)) > 0 && !detector.isDone()) {
          detector.handleData(buf, 0, readCount);
        }
      }
      detector.dataEnd();

      Charset encoding = Charsets.UTF_8;
      try {
        String detectedEncoding = detector.getDetectedCharset();
        if (detectedEncoding != null) {
          encoding = Charset.forName(detectedEncoding);
        }
      } catch (UnsupportedCharsetException e) {
        //ignore, we use UTF-8 as a fallback
      }
      return new InputStreamReader(new FileInputStream(file), encoding);
    }

  }

  @Override
  public Optional<MediaType> getMimeType() {
    return mimeType;
  }

  @Override
  public void close() throws Exception {

  }
}

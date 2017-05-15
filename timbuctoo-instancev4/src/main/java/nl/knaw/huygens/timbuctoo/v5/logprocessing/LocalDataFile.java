package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Optional;

public class LocalDataFile implements LocalData {

  private final URI name;
  private final File file;
  private final Optional<Charset> encoding;
  private final Optional<String> mimeType;

  @JsonCreator
  public LocalDataFile(@JsonProperty("name") URI name, @JsonProperty("file") File file,
                       @JsonProperty("encoding") Optional<String> encoding,
                       @JsonProperty("mimeType") Optional<String> mimeType)
      throws UnsupportedEncodingException {
    this.name = name;
    this.file = file;
    this.encoding = encoding.map(Charset::forName);
    this.mimeType = mimeType;
  }

  @Override
  public String getContentAddress() {
    return this.file.getAbsolutePath();
  }

  @Override
  public URI getUri() {
    return name;
  }

  @Override
  @JsonIgnore
  public Reader getReader() throws IOException {
    if (encoding.isPresent()) {
      return new InputStreamReader(new FileInputStream(file), encoding.get());
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
  @JsonIgnore
  public Writer getAppendingWriter() throws FileNotFoundException {
    return new OutputStreamWriter(new FileOutputStream(file, true), Charsets.UTF_8);
  }

  @Override
  public Optional<String> getMimeType() {
    return mimeType;
  }

  public File getFile() {
    return file;
  }

  public Optional<String> getEncoding() {
    return encoding.map(Charset::name);
  }
}

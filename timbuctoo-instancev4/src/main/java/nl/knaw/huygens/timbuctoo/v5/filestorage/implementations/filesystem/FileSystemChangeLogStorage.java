package nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem;

import nl.knaw.huygens.timbuctoo.v5.filestorage.ChangeLogStorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

public class FileSystemChangeLogStorage implements ChangeLogStorage {
  private final File dir;

  public FileSystemChangeLogStorage(File dir) {
    this.dir = dir;
    dir.mkdirs();
  }

  @Override
  public OutputStream getChangeLogOutputStream(int version) throws IOException {
    return new GZIPOutputStream(new FileOutputStream(new File(dir, String.valueOf(version))));
  }

  @Override
  public Optional<File> getChangeLog(int version) {
    File file = new File(dir, String.valueOf(version));
    if (file.exists()) {
      return Optional.of(file);
    }
    return Optional.empty();
  }

  @Override
  public void clear() throws IOException {
    synchronized (dir) {
      for (File file : dir.listFiles()) {
        file.delete();
      }
    }
  }
}

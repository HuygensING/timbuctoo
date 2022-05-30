package nl.knaw.huygens.timbuctoo.server.tasks;

import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressFilesTask extends Task {
  private final DataSetRepository dataSetRepository;

  public CompressFilesTask(DataSetRepository dataSetRepository) {
    super("compressFiles");
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
    for (DataSet dataSet : dataSetRepository.getDataSets()) {
      for (LogEntry logEntry : dataSet.getImportManager().getLogList().getEntries()) {
        if (logEntry.getLogToken().isPresent()) {
          try (CachedLog log = dataSet.getLogStorage().getLog(logEntry.getLogToken().get())) {
            if (log.getCharset() != null) {
              File file = log.getFile();

              RandomAccessFile raf = new RandomAccessFile(file, "r");
              int magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
              raf.close();

              if (magic != GZIPInputStream.GZIP_MAGIC) {
                File tmp = File.createTempFile("gzipped", null);
                try (InputStream inputStream = new FileInputStream(file);
                     OutputStream outputStream = new GZIPOutputStream(new FileOutputStream(tmp))) {
                  inputStream.transferTo(outputStream);
                }

                Files.copy(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                tmp.delete();
              }
            }
          }
        }
      }
    }
  }
}

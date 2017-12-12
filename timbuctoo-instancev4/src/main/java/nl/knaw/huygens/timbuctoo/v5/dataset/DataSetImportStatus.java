package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.EntryImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Report status of imports on a dataSet.
 */
public class DataSetImportStatus {

  private final LogList logList;

  public DataSetImportStatus(LogList logList) {
    this.logList = logList;
  }

  public String getStatus() {
    return logList.getLastStatus();
  }

  public String getLastImportDate() {
    return logList.getLastImportDate();
  }

  public long getLastImportDuration(String unit) {
    if (logList.getLastImportDuration().isPresent()) {
      return  logList.getLastImportDuration().get().getTime(unit);
    } else {
      return -1L;
    }
  }

  public List<String> getDataSetErrors() {
    return logList.getListErrors();
  }

  public List<String> getEntryErrors() {
    return logList.getEntries().stream()
                  .map(LogEntry::getImportStatus)
                  .flatMap(eis -> eis.isPresent() ? eis.get().getErrors().stream() : Stream.empty())
                  .collect(Collectors.toList());
  }

  public int getTotalErrorCount() {
    return getDataSetErrors().size() + getEntryErrors().size();
  }

  public List<EntryImportStatus> getEntryImports() {
    return logList.getEntries().stream()
      .map(LogEntry::getImportStatus)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
  }

  public List<EntryImportStatus> getEntryImportsWithErrors() {
    return logList.getEntries().stream()
                  .map(LogEntry::getImportStatus)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .filter(eis -> eis.getErrors().size() > 0)
                  .collect(Collectors.toList());
  }


  // @ToDo LogEntries have no Id
  // public EntryImportStatus getEntryImportStatus(String id) {
  //   return null;
  // }

}

package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.synchronizedList;

public class DataSet {

  private final String name;
  private final List<LogPart> logParts;

  public DataSet(String name) {
    this(name, Lists.newArrayList());
  }

  public DataSet(String name, List<LogPart> logParts) {
    this.name = name;
    this.logParts = synchronizedList(logParts);
  }

  public String getName() {
    return this.name;
  }

  public boolean isUpToDate() {
    return logParts.stream().allMatch(LogPart::isUpToDate);
  }

  public DataSetStatus getStatus() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  public void addLogPart(LogPart logPart) {
    logParts.add(logPart);
  }

  public Optional<LogPart> nextLogToProcess() {
    return logParts.stream().filter(logPart -> !logPart.isUpToDate()).findFirst();
  }

  class DataSetStatus {

  }
}

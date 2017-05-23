package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.synchronizedList;

public class DataSet {

  private final String name;
  private final List<RdfLogEntry> rdfLogEntries;

  public DataSet(String name) {
    this(name, Lists.newArrayList());
  }

  public DataSet(String name, List<RdfLogEntry> rdfLogEntries) {
    this.name = name;
    this.rdfLogEntries = synchronizedList(rdfLogEntries);
  }

  public String getName() {
    return this.name;
  }

  public boolean isUpToDate() {
    return rdfLogEntries.stream().allMatch(RdfLogEntry::isUpToDate);
  }

  public DataSetStatus getStatus() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  public void addLogPart(RdfLogEntry rdfLogEntry) {
    rdfLogEntries.add(rdfLogEntry);
  }

  public Optional<RdfLogEntry> nextLogToProcess() {
    return rdfLogEntries.stream().filter(logPart -> !logPart.isUpToDate()).findFirst();
  }

  class DataSetStatus {

  }
}

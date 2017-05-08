package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.ProcessingFailedException;

public interface DataProcessor<T> {
  void process(T source, long version) throws ProcessingFailedException;
}

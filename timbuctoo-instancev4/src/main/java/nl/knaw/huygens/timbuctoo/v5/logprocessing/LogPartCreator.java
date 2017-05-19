package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.LogPart.ProcessStep;

public class LogPartCreator {

  public LogPart rdfLogPart() {


    return new LogPart(Lists.newArrayList());
  }

  private static class SafeToTrippleStoreProcessStep implements ProcessStep {

    @Override
    public int getLastLineDone() {
      return 0;
    }

    @Override
    public LogPart.ImportStepStatus getStatus() {
      return null;
    }

    @Override
    public void execute() {

    }
  }

  private static class SafeToCollectionIndexProcessStep implements ProcessStep {
    @Override
    public int getLastLineDone() {
      return 0;
    }

    @Override
    public LogPart.ImportStepStatus getStatus() {
      return null;
    }

    @Override
    public void execute() {

    }
  }

  private static class SafeToTypeNameStoreIndexProcessStep implements ProcessStep {
    @Override
    public int getLastLineDone() {
      return 0;
    }

    @Override
    public LogPart.ImportStepStatus getStatus() {
      return null;
    }

    @Override
    public void execute() {

    }
  }
}

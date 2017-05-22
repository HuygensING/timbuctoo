package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class DataSetStubs {
  public static DataSet dataSetWithName(String name) {
    DataSet dataSet = mock(DataSet.class);
    given(dataSet.getName()).willReturn(name);
    return dataSet;
  }

  public static DataSet dataSetWithNameAndStatus(String name, DataSet.DataSetStatus dataSetStatus) {
    DataSet dataSet = mock(DataSet.class);
    given(dataSet.getName()).willReturn(name);
    given(dataSet.getStatus()).willReturn(dataSetStatus);
    return dataSet;
  }

  public static DataSet dataSetWithLogs(List<LogPart> logParts) {
    return new DataSet("name", logParts);
  }
}

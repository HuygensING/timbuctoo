package nl.knaw.huygens.timbuctoo.server.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetConfiguration;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.BasicDataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.OldDataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.IllegalDataSetNameException;
import nl.knaw.huygens.timbuctoo.v5.filehelper.FileHelper;
import nl.knaw.huygens.timbuctoo.v5.jacksonserializers.TimbuctooCustomSerializers;
import nl.knaw.huygens.timbuctoo.v5.jsonfilebackeddata.JsonFileBackedData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MetaDataMigration {
  private DataSetConfiguration configuration;

  public MetaDataMigration(DataSetConfiguration configuration) {
    this.configuration = configuration;
  }

  public void migrate() throws IOException, IllegalDataSetNameException {
    FileHelper fileHelper = new FileHelper(configuration.getDataSetMetadataLocation());
    JsonFileBackedData<Map<String, Set<OldDataSetMetaData>>> storedDataSets = JsonFileBackedData.getOrCreate(
      new File(configuration.getDataSetMetadataLocation(), "dataSets.json"),
      HashMap::new,
      new TypeReference<Map<String, Set<OldDataSetMetaData>>>() {
      });

    ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new Jdk8Module())
      .registerModule(new GuavaModule())
      .registerModule(new TimbuctooCustomSerializers())
      .enable(SerializationFeature.INDENT_OUTPUT);


    for (Set<OldDataSetMetaData> metaDataSet : storedDataSets.getData().values()) {
      for (OldDataSetMetaData olddataSetMetaData : metaDataSet) {
        BasicDataSetMetaData dataSetMetaData = olddataSetMetaData.convertToDataSetMetaData();

        File metaDataFile = fileHelper.fileInDataSet(dataSetMetaData.getOwnerId(), dataSetMetaData.getDataSetId(),
          "/metaData.json");

        objectMapper.writeValue(metaDataFile, dataSetMetaData);
      }
    }
  }


}

package nl.knaw.huygens.repository.importer.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import nl.knaw.huygens.repository.model.Document;

public interface ResultSetConverter<T extends Document> {
  List<T> convert(ResultSet resultSet) throws SQLException;
}

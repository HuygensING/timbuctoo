package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bson.BSONCallback;
import org.bson.BSONObject;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBCallback;
import com.mongodb.DBCollection;
import com.mongodb.DBDecoder;
import com.mongodb.DBObject;

import de.undercouch.bson4jackson.BsonParser;

public class TreeDecoder implements DBDecoder {

  @Override
  public BSONObject readObject(byte[] b) {
    return decode(b, (DBCollection) null);
  }

  @Override
  public BSONObject readObject(InputStream in) throws IOException {
    return decode(in, (DBCollection) null);
  }

  @Override
  public int decode(byte[] b, BSONCallback callback) {
    throw new UnsupportedOperationException("Jackson TreeDecoder does not support callback style decoding");
  }

  @Override
  public int decode(InputStream in, BSONCallback callback) throws IOException {
    throw new UnsupportedOperationException("Jackson TreeDecoder does not support callback style decoding");
  }

  @Override
  public DBCallback getDBCallback(DBCollection collection) {
    throw new UnsupportedOperationException("Jackson TreeDecoder does not support callback style decoding");
  }

  @Override
  public DBObject decode(byte[] b, DBCollection collection) {
    try {
      return decode(new ByteArrayInputStream(b), collection);
    } catch (IOException e) {
      // Not possible
      throw new RuntimeException("IOException encountered while reading from a byte array input stream", e);
    }
  }

  @Override
  public DBObject decode(InputStream in, DBCollection collection) throws IOException {
    BsonParser jp = new BsonParser(new IOContext(new BufferRecycler(), in, false), 0, BsonParser.Feature.HONOR_DOCUMENT_LENGTH.getMask(), in);
    jp.setCodec(new ObjectMapper());
    return new DBJsonNode((JsonNode) jp.readValueAsTree());
  }

}

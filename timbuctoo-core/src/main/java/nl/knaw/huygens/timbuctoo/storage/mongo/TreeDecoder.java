package nl.knaw.huygens.timbuctoo.storage.mongo;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
    JsonNode tree = (JsonNode) jp.readValueAsTree();
    jp.close();
    return new DBJsonNode(tree);
  }

}

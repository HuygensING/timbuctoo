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

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;
import nl.knaw.huygens.timbuctoo.storage.RevisionChanges;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Lists;

public class MongoChanges<T extends Entity> implements RevisionChanges<T> {

  public MongoChanges(String id, T item) {
    this._id = id;
    this.versions = Lists.newArrayListWithExpectedSize(1);
    this.versions.add(item);
  }

  protected MongoChanges() {
    // do nothing, used for Jackson
  }

  public String _id;

  @JsonDeserialize(contentUsing = BSONDeserializer.class)
  public List<T> versions;

  @Override
  @JsonIgnore
  public String getId() {
    return _id;
  }

  @JsonView(JsonViews.WebView.class)
  @Override
  public List<T> getRevisions() {
    return versions;
  }

}

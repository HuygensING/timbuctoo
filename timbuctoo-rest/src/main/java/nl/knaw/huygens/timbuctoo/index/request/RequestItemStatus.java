package nl.knaw.huygens.timbuctoo.index.request;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import com.google.common.collect.Lists;

import java.util.List;

class RequestItemStatus {
  private List<String> toDo;
  private List<String> done;

  RequestItemStatus() {
    done = Lists.newArrayList();
    toDo = Lists.newArrayList();
  }

  public void setToDo(List<String> toDo) {
    this.toDo.addAll(toDo);
  }

  public void done(String id) {
    if (toDo.remove(id)) {
      done.add(id);
    }
  }

  public List<String> getToDo() {
    return Lists.newArrayList(toDo);
  }

  public List<String> getDone() {
    return Lists.newArrayList(done);
  }
}

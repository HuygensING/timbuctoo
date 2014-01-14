package nl.knaw.huygens.timbuctoo.messages;

/*
 * #%L
 * Timbuctoo services
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class Action {

  private final ActionType actionType;
  private final String id;
  private final Class<? extends DomainEntity> type;

  public Action(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    this.actionType = actionType;
    this.id = id;
    this.type = type;
  }

  public ActionType getActionType() {
    return actionType;
  }

  public String getId() {
    return id;
  }

  public Class<? extends DomainEntity> getType() {
    return type;
  }

  @Override
  public String toString() {
    return "{\nactionType: " + actionType + "\ntypeString: " + type + "\nid: " + id + "\n}";
  }

}

package nl.knaw.huygens.timbuctoo.messages;

/*
 * #%L
 * Timbuctoo services
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

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class ActionMatcher extends CompositeMatcher<Action> {

  private ActionMatcher() {

  }

  public static ActionMatcher likeAction() {
    return new ActionMatcher();
  }

  public ActionMatcher withActionType(ActionType actionType) {
    this.addMatcher(new PropertyEqualityMatcher<Action, ActionType>("actionType", actionType) {
      @Override
      protected ActionType getItemValue(Action item) {
        return item.getActionType();
      }
    });
    return this;
  }

  public ActionMatcher withType(Class<? extends DomainEntity> type) {
    this.addMatcher(new PropertyEqualityMatcher<Action, Class<? extends DomainEntity>>("type", type) {
      @Override
      protected Class<? extends DomainEntity> getItemValue(Action item) {
        return item.getType();
      }
    });
    return this;
  }

  public ActionMatcher withId(String id) {
    this.addMatcher(new PropertyEqualityMatcher<Action, String>("id", id) {
      @Override
      protected String getItemValue(Action item) {
        return item.getId();
      }
    });
    return this;
  }

  public ActionMatcher withForMultiEntitiesFlag(boolean forMultiEntitiesFlag){
    this.addMatcher(new PropertyEqualityMatcher<Action, Boolean>("forMultiEntities", forMultiEntitiesFlag) {
      @Override
      protected Boolean getItemValue(Action item) {
        return item.isForMultiEntities();
      }
    });
    return this;
  }
}

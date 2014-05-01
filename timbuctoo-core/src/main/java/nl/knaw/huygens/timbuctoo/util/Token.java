package nl.knaw.huygens.timbuctoo.util;

/*
 * #%L
 * Timbuctoo core
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

import java.io.Serializable;

/**
 * Represents a token in a text. This may be a word, or an n-gram.
 */
public class Token implements Serializable {

  private static final long serialVersionUID = 1L;

  /** The text identifying the token. */
  private final String text;
  /** The number of occurrences of this token. */
  private int count;
  /** A (statistical) value associated with this token. */
  private double value;

  public Token(String text) {
    this.text = text;
    count = 0;
    value = 0.0;
  }

  public String getText() {
    return text;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public void decrement() {
    count--;
  }

  public void increment(int value) {
    count += value;
  }

  public void increment() {
    count++;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

}

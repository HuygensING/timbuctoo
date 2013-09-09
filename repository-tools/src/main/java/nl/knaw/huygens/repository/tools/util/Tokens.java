package nl.knaw.huygens.repository.tools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A collection of tokens.
 */
public class Tokens implements Serializable {

  private static final long serialVersionUID = 1L;

  private Map<String, Token> tokens;

  public Tokens() {
    tokens = Maps.newHashMap();
  }

  public Token get(String key) {
    return tokens.get(key);
  }

  public void put(Token token) {
    tokens.put(token.getText(), token);
  }

  public void increment(String key, int value) {
    Token token = tokens.get(key);
    if (token == null) {
      token = new Token(key);
      tokens.put(key, token);
    }
    token.increment(value);
  }

  public void increment(String key) {
    Token token = tokens.get(key);
    if (token == null) {
      token = new Token(key);
      tokens.put(key, token);
    }
    token.increment();
  }

  public int getTotalTokenCount() {
    int total = 0;
    for (Map.Entry<String, Token> entry : tokens.entrySet()) {
      total += entry.getValue().getCount();
    }
    return total;
  }

  public int getUniqueTokenCount() {
    return tokens.size();
  }

  public int getCountFor(String key) {
    Token token = tokens.get(key);
    return (token != null) ? token.getCount() : 0;
  }

  public double getValueFor(String key) {
    Token token = tokens.get(key);
    return (token != null) ? token.getValue() : 0.0;
  }

  public void handleSortedByCount(TokenHandler handler) {
    handleSorted(handler, new Comparator<Token>() {
      @Override
      public int compare(Token token1, Token token2) {
        int diff = token2.getCount() - token1.getCount();
        if (diff < 0) {
          return -1;
        } else if (diff > 0) {
          return +1;
        } else {
          return 0;
        }
      }
    });
  }

  public void handleSortedByValue(TokenHandler handler) {
    handleSorted(handler, new Comparator<Token>() {
      @Override
      public int compare(Token token1, Token token2) {
        double diff = token2.getValue() - token1.getValue();
        if (diff < 0) {
          return -1;
        } else if (diff > 0) {
          return +1;
        } else {
          return 0;
        }
      }
    });
  }

  public void handleSortedByText(TokenHandler handler) {
    handleSorted(handler, new Comparator<Token>() {
      @Override
      public int compare(Token token1, Token token2) {
        return token1.getText().compareTo(token2.getText());
      }
    });
  }

  public void handleSorted(TokenHandler handler, Comparator<Token> comparator) {
    List<Token> list = Lists.newArrayList(tokens.values());
    Collections.sort(list, comparator);
    for (Token token : list) {
      if (!handler.handle(token)) {
        break;
      }
    }
  }

  public void handle(TokenHandler handler) {
    for (Token token : tokens.values()) {
      if (!handler.handle(token)) {
        break;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void read(File file) throws IOException {
    try {
      ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
      tokens = (Map<String, Token>) stream.readObject();
      stream.close();
    } catch (ClassNotFoundException e) {
      throw new IOException("Failed to read tokens", e);
    }
  }

  public void write(File file) throws IOException {
    ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
    stream.writeObject(tokens);
    stream.close();
  }

}

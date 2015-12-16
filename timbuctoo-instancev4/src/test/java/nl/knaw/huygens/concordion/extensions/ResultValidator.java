package nl.knaw.huygens.concordion.extensions;

public interface ResultValidator {
  String validate(HttpExpectation expectation, HttpResult reality);
}

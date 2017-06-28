package edu.wpi.first.outlineviewer.model;

/**
 * An entry containing a single number.
 */
public class NumberEntry extends Entry<Number> {

  public NumberEntry(String key, Number value) {
    super(key, value);
  }

  @Override
  protected String getTypeString(Number value) {
    return "Number";
  }
}
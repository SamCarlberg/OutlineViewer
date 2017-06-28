package edu.wpi.first.outlineviewer.model;

/**
 * An entry containing a single boolean.
 */
public class BooleanEntry extends Entry<Boolean> {

  public BooleanEntry(String key, Boolean value) {
    super(key, value);
  }

  @Override
  protected String getTypeString(Boolean value) {
    return "Boolean";
  }

}
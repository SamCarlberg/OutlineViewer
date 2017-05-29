package edu.wpi.first.tableviewer;

import edu.wpi.first.tableviewer.entry.Entry;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;

/**
 * TreeTableCell implementation that uses different editors based on the type of data in the cell.
 */
class TableEntryTreeTableCell extends TreeTableCell<Entry, Object> {

  private Class<?> type = null;
  private Control editor = null;
  private Node graphic = null;
  private String text = null;
  private boolean canEdit = false;

  public TableEntryTreeTableCell() {
    setEditable(true);
  }

  @Override
  protected void updateItem(Object item, boolean empty) {
    super.updateItem(item, empty);
    if (item == null || empty || getTreeTableRow().getTreeItem() == null) {
      setText("");
      setGraphic(null);
      return;
    }
    Entry entry = getTreeTableRow()
        .getTreeItem()
        .getValue();
    type = item.getClass();
    setGraphic(null);
    setText(null);

    canEdit = true; // assume it's editable; if it's not, we'll set it later
    if (item instanceof Boolean) {
      CheckBox checkBox = new CheckBox();
      checkBox.setSelected((Boolean) item);
      editor = checkBox;
      setGraphic(checkBox);
      checkBox.setOnAction(event -> {
        if (!isEditing())
          getTreeTableRow().getTreeTableView().edit(getTreeTableRow().getIndex(), getTableColumn());
        commitEdit(checkBox.isSelected());
      });
    } else if (item instanceof String) {
      TextField field = new TextField((String) item);
      field.setOnAction(e -> commitEdit(field.getText()));
      editor = field;
    } else if (item instanceof Number) {
      TextField field = new TextField(item.toString());
      field.setOnAction(e -> {
        try {
          commitEdit(Double.parseDouble(field.getText()));
        } catch (NumberFormatException ignore) {
          field.setText(item.toString());
          cancelEdit();
        }
      });
      editor = field;
    } else {
      // not editable
      System.out.println("Not editable: " + item);
      canEdit = false;
    }
    setText(entry.getDisplayString());
  }

  @Override
  public void startEdit() {
    if (!canEdit) {
      return;
    }
    super.startEdit();
    graphic = getGraphic();
    text = getText();
    setGraphic(editor);
    if (!(editor instanceof CheckBox)) {
      setText(null);
    }
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();
    setGraphic(graphic);
    setText(text);
  }

  @Override
  public void commitEdit(Object newValue) {
    super.commitEdit(newValue);
    setGraphic(graphic);
    setText(text);
  }

}
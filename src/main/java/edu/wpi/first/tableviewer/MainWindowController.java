package edu.wpi.first.tableviewer;

import edu.wpi.first.tableviewer.component.NetworkTableTree;
import edu.wpi.first.tableviewer.dialog.AddBooleanDialog;
import edu.wpi.first.tableviewer.dialog.AddNumberDialog;
import edu.wpi.first.tableviewer.dialog.AddStringDialog;
import edu.wpi.first.tableviewer.dialog.Dialogs;
import edu.wpi.first.tableviewer.dialog.PreferencesDialog;
import edu.wpi.first.tableviewer.entry.Entry;
import edu.wpi.first.tableviewer.entry.TableEntry;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static edu.wpi.first.tableviewer.NetworkTableUtils.concat;
import static edu.wpi.first.tableviewer.NetworkTableUtils.isPersistent;
import static edu.wpi.first.tableviewer.NetworkTableUtils.simpleKey;

/**
 *
 */
public class MainWindowController {

  @FXML
  private Pane root;

  @FXML
  private NetworkTableTree tableView;
  @FXML
  private TreeItem<Entry> ntRoot;

  @FXML
  private TreeTableColumn<Entry, String> keyColumn;
  @FXML
  private TreeTableColumn<Entry, Object> valueColumn;
  @FXML
  private TreeTableColumn<Entry, String> typeColumn;

  @FXML
  private ToolBar searchBar;
  @FXML
  private Button closeSearchButton;
  @FXML
  private TextField searchField;

  private final Predicate<Entry> metadataFilter = x -> Prefs.isShowMetaData() || !x.isMetadata();

  @FXML
  private void initialize() {
    root.setOnKeyPressed(event -> {
      if (event.isControlDown() && event.getCode() == KeyCode.F) {
        searchBar.setManaged(true);
        Platform.runLater(searchField::requestFocus);
        searchField.selectAll();
      }
      if (event.getCode() == KeyCode.ESCAPE) {
        searchField.setText("");
        searchBar.setManaged(false);
      }
    });

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    tableView.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE) {
        deleteSelectedEntries();
      }
    });

    tableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);


    keyColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(simpleKey(param.getValue().getValue().getKey())));
    valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));
    typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));

    valueColumn.setCellFactory(param -> new TableEntryTreeTableCell());

    valueColumn.setOnEditCommit(e -> {
      Entry entry = e.getRowValue().getValue();
      String key = entry.getKey(); // entry keys are guaranteed to be normalized
      // Use raw object put from NetworkTable API (JNI doesn't support it)
      NetworkTable.getTable(key.substring(0, key.lastIndexOf('/'))).putValue(simpleKey(key), e.getNewValue());
    });

    tableView.setRowFactory(param -> {
      final TreeTableRow<Entry> row = new TreeTableRow<>();
      // Clicking on an empty row should clear the selection.
      row.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
        if (row.getTreeItem() == null) {
          tableView.getSelectionModel().clearSelection();
          event.consume();
        }
      });
      return row;
    });

    tableView.setFilter(metadataFilter);

    searchField.textProperty().addListener((__, oldText, newText) -> {
      if (newText.isEmpty()) {
        tableView.setFilter(metadataFilter);
      } else {
        String lower = newText.toLowerCase();
        Predicate<Entry> filter = metadataFilter.and(data -> data.getKey().toLowerCase().contains(lower)
            || data.getDisplayString().toLowerCase().contains(lower)
            || data.getType().toLowerCase().contains(lower));
        tableView.setFilter(filter);
      }
    });

    tableView.setOnMouseClicked(e -> {
      if (e.getClickCount() == 2) {
        TreeItem<Entry> selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
          return;
        }
        int row = tableView.getRow(selected);
        tableView.edit(row, valueColumn);
      }
    });

    tableView.setOnContextMenuRequested(e -> {
      if (tableView.getContextMenu() != null) {
        // Close previous context menu
        tableView.getContextMenu().hide();
      }
      // The actions in the menu only affect one entry,
      // so we only select the entry that was clicked on.
      if (tableView.getSelectionModel().getSelectedItems().size() > 1) {
        tableView.getSelectionModel().clearAndSelect(tableView.getSelectionModel().getSelectedIndex());
      }
      TreeItem<Entry> selected = tableView.getSelectionModel().getSelectedItem();
      if (selected == null) {
        return;
      }
      Entry entry = selected.getValue();
      String key = entry.getKey();
      ContextMenu cm = new ContextMenu();

      if (entry instanceof TableEntry) {
        // It's a table, add the 'add x' items
        cm.getItems().addAll(createTableMenuItems(entry));
        cm.getItems().add(new SeparatorMenuItem());
      }

      if (!key.isEmpty() && entry.getValue() != null) {
        MenuItem setPersistent = new MenuItem(String.format("Set %s", isPersistent(key) ? "transient" : "persistent"));
        setPersistent.setOnAction(__ -> NetworkTableUtils.togglePersistent(key));

        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(__ -> deleteSelectedEntries());

        cm.getItems().addAll(setPersistent, delete);
      } else {
        // Remove the separator
        cm.getItems().remove(cm.getItems().size() - 1);
      }

      tableView.setContextMenu(cm);
      cm.show(tableView, e.getScreenX(), e.getScreenY());
    });
    Prefs.showMetaDataProperty().addListener(__ -> tableView.updateItemsFromFilter());
  }

  private List<MenuItem> createTableMenuItems(Entry<?> tableEntry) {
    final String key = tableEntry.getKey();

    MenuItem string = new MenuItem("Add string");
    string.setOnAction(a -> {
      new AddStringDialog().showAndWait().ifPresent(data -> {
        String k = concat(key, data.getKey());
        NetworkTablesJNI.putString(k, data.getValue());
      });
    });

    MenuItem number = new MenuItem("Add number");
    number.setOnAction(a -> {
      new AddNumberDialog().showAndWait().ifPresent(data -> {
        String k = concat(key, data.getKey());
        NetworkTablesJNI.putDouble(k, data.getValue().doubleValue());
      });
    });

    MenuItem bool = new MenuItem("Add boolean");
    bool.setOnAction(a -> {
      new AddBooleanDialog().showAndWait().ifPresent(data -> {
        String k = concat(key, data.getKey());
        NetworkTablesJNI.putBoolean(k, data.getValue());
      });
    });

    return Arrays.asList(string, number, bool);
  }

  /**
   * Deletes all selected entries.
   */
  private void deleteSelectedEntries() {
    tableView.getSelectionModel()
             .getSelectedItems()
             .stream()
             .map(item -> item.getValue())
             .map(entry -> entry.getKey())
             .forEach(key -> NetworkTableUtils.delete(key));
  }

  @FXML
  private void close() {
    System.exit(0);
  }

  @FXML
  private void clearSearch() {
    searchField.setText("");
    searchField.requestFocus();
  }

  @FXML
  private void showPrefs() throws IOException {
    PreferencesDialog dialog = new PreferencesDialog("Preferences", ButtonType.FINISH);
    Platform.runLater(() -> Dialogs.center(dialog.getDialogPane().getScene().getWindow()));
    dialog.showAndWait()
          .map(ButtonType.FINISH::equals)
          .ifPresent(__ -> dialog.getController().start());
  }

}
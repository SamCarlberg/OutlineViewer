package edu.wpi.first.outlineviewer.view.dialog;

import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.testfx.matcher.control.ListViewMatchers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AddStringArrayDialogTest extends AddEntryArrayDialogTest {

  public AddStringArrayDialogTest() {
    super(AddStringArrayDialog::new);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testInitialValue() {
    final String[] test = new String[]{"", "A String", "And another!"};
    ((AddEntryArrayDialog) dialog).setInitial(test);

    assertArrayEquals(test, ((ListView) lookup(".list-view").query()).getItems().toArray());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGetData() {
    final String[] test = new String[]{"", "A String", "And another!"};
    ((AddEntryArrayDialog) dialog).setInitial(test);

    assertArrayEquals(test, (String[]) dialog.getData());
  }

  @Test
  public void testToStringConverter() {
    final String test = "A String!";
    ListView listView = lookup(ListViewMatchers.isEmpty()).query();
    clickOn("+");

    doubleClickOn((Node) from(listView).lookup(".list-cell").query()).press(KeyCode.DELETE)
        .write(test).type(KeyCode.ENTER);

    assertEquals(test, listView.getItems().get(0));
  }

}
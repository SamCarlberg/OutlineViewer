package edu.wpi.first.tableviewer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class PreferencesControllerTest extends ApplicationTest {

  private PreferencesController controller;

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(PreferencesController.class.getResource("Preferences.fxml"));
    Pane prefsPane = loader.load();
    controller = loader.getController();
    stage.setScene(new Scene(prefsPane));
    stage.show();
    Prefs.setServer(true);
    Prefs.setPort(1735);
    Prefs.setIp("localhost");
    Prefs.setShowMetaData(true);
  }

  @Test
  public void testServerIdDisabledInServerMode() {
    Prefs.setServer(true);
    TextField idField = lookup(n -> "idField".equals(n.getId())).query();
    assertTrue(idField.isDisable());
  }

  @Test
  public void testServerIdEnabledInClientMode() {
    Prefs.setServer(false);
    TextField idField = lookup(n -> "idField".equals(n.getId())).query();
    assertFalse(idField.isDisable());
  }

  @Test
  public void testNoAddressInterpretedAsLocalhost() {
    Prefs.setServer(false);
    TextField idField = lookup(n -> "idField".equals(n.getId())).query();
    FxHelper.runAndWait(() -> idField.setText(""));
    controller.start();
    assertEquals("localhost", Prefs.getIp());
  }

  @Test
  public void testServerPortInServerMode() {
    Prefs.setServer(true);
    TextField portField = lookup(n -> "portField".equals(n.getId())).query();
    FxHelper.runAndWait(() -> portField.setText("9999"));
    waitForFxEvents();
    controller.start();
    assertEquals(9999, Prefs.getPort());
  }

  @Test
  public void testServerPortInClientMode() {
    Prefs.setServer(false);
    TextField portField = lookup(n -> "portField".equals(n.getId())).query();
    FxHelper.runAndWait(() -> portField.setText("2084"));
    waitForFxEvents();
    controller.start();
    assertEquals(2084, Prefs.getPort());
  }

  @Test
  public void testServerSwitchInital() {
    ToggleSwitch serverSwitch = lookup(n -> "modeSwitch".equals(n.getId())).query();
    assertEquals(serverSwitch.isSelected(), Prefs.isServer());
  }

  @Test
  public void testServerSwitchChange() {
    ToggleSwitch serverSwitch = lookup(n -> "modeSwitch".equals(n.getId())).query();
    FxHelper.runAndWait(() -> serverSwitch.setSelected(!serverSwitch.isSelected()));
    assertEquals(serverSwitch.isSelected(), Prefs.isServer());
  }

  @Test
  public void testMetadataSwitchInital() {
    ToggleSwitch metadataSwitch = lookup(n -> "metadataSwitch".equals(n.getId())).query();
    assertEquals(Prefs.isShowMetaData(), metadataSwitch.isSelected());
  }

  @Test
  public void testMetadataSwitchChange() {
    ToggleSwitch metadataSwitch = lookup(n -> "metadataSwitch".equals(n.getId())).query();
    FxHelper.runAndWait(() -> metadataSwitch.setSelected(!metadataSwitch.isSelected()));
    assertEquals(metadataSwitch.isSelected(), Prefs.isShowMetaData());
  }

}

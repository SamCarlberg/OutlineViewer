package edu.wpi.first.tableviewer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.prefs.Preferences;

/**
 * App-global preferences.
 */
public class Prefs {

  public static final String SHOW_METADATA = "show_metadata";
  public static final String SERVER = "server";
  public static final String IP = "ip";
  public static final String RESOLVED_ADDRESS = "resolved_address";

  private static final Preferences preferences = Preferences.userNodeForPackage(Main.class);

  /**
   * Whether or not metadata should be visible in the tree. Defaults to true.
   */
  private static final BooleanProperty showMetaData
      = new SimpleBooleanProperty(Prefs.class, SHOW_METADATA, true);

  /**
   * Whether or not the app should be running in server mode. Defaults to false (client mode).
   */
  private static final BooleanProperty server
      = new SimpleBooleanProperty(Prefs.class, SERVER, false);

  /**
   * The address given by the user. This is coerced into a port (if applicable)
   * and a {@link #resolvedAddress resolved address}. For example, if this is "192" and the app
   * is in client mode, the resolved address is "roborio-192-frc.local". If this is "192" and the
   * app is in server mode, the resolved address is not affected but the port is set to 192. If
   * this is "localhost:192" and the app is in client mode, the resolved address will be localhost
   * and the remote server port will be set to 192.
   */
  private static final StringProperty ip
      = new SimpleStringProperty(Prefs.class, IP, "localhost");


  /**
   * The actual address for the client to connect to. Does nothing if the app is in server mode.
   *
   * @see #server
   */
  private static final StringProperty resolvedAddress
      = new SimpleStringProperty(Prefs.class, RESOLVED_ADDRESS, null);

  // Load saved preferences and set up listeners to automatically save changes
  static {
    setShowMetaData(preferences.getBoolean(SHOW_METADATA, false));
    setServer(preferences.getBoolean(SERVER, false));
    setIp(preferences.get(IP, "localhost"));
    setResolvedAddress(preferences.get(RESOLVED_ADDRESS, "localhost"));

    showMetaDataProperty().addListener((__, o, n) -> preferences.putBoolean(SHOW_METADATA, n));
    serverProperty().addListener((__, o, n) -> preferences.putBoolean(SERVER, n));
    ipProperty().addListener((__, o, n) -> preferences.put(IP, n));
    resolvedAddressProperty().addListener((__, o, n) -> preferences.put(RESOLVED_ADDRESS, n));
  }

  private Prefs() {
  }

  public static boolean isShowMetaData() {
    return showMetaData.get();
  }

  public static BooleanProperty showMetaDataProperty() {
    return showMetaData;
  }

  public static void setShowMetaData(boolean showMetaData) {
    Prefs.showMetaData.set(showMetaData);
  }

  public static boolean isServer() {
    return server.get();
  }

  public static BooleanProperty serverProperty() {
    return server;
  }

  public static void setServer(boolean server) {
    Prefs.server.set(server);
  }

  public static String getIp() {
    return ip.get();
  }

  public static StringProperty ipProperty() {
    return ip;
  }

  public static void setIp(String ip) {
    Prefs.ip.set(ip);
  }

  public static String getResolvedAddress() {
    return resolvedAddress.get();
  }

  public static StringProperty resolvedAddressProperty() {
    return resolvedAddress;
  }

  public static void setResolvedAddress(String resolvedAddress) {
    Prefs.resolvedAddress.set(resolvedAddress);
  }

}

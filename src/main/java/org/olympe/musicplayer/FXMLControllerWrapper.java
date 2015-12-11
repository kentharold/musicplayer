package org.olympe.musicplayer;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import jfxtras.labs.util.Util;
import org.olympe.musicplayer.util.FileNameStringConverter;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FXMLControllerWrapper {

    private final MusicPlayerController controller;
    private final Stage stage;

    private double mouseDragOffsetX = 0;
    private double mouseDragOffsetY = 0;

    @FXML
    private ListView<File> musicFilesView;
    @FXML
    private Button prevTrackBtn;
    @FXML
    private ToggleButton playPauseBtn;
    @FXML
    private Button nextTractBtn;
    @FXML
    private Label currentTimeLbl;
    @FXML
    private Slider durationSlider;
    @FXML
    private Label totalTimeLbl;
    @FXML
    private ToggleButton muteToggleBtn;
    @FXML
    private Slider volumeSlider;
    @FXML
    private CheckBox repeatToggleBtn;
    @FXML
    private StackPane overlayPane;
    @FXML
    private StackPane root;
    @FXML
    private StackPane backgroundPane;
    @FXML
    private ImageView coverView;

    private Preferences appPreferences;
    private Preferences viewPreferences;
    private Preferences playerPreferences;

    public FXMLControllerWrapper(MusicPlayerController controller, Stage primaryStage) {
        this.controller = controller;
        this.stage = primaryStage;
        initPreferences();
    }

    public MusicPlayerController getController() {
        return controller;
    }

    @FXML
    void initialize() {
        musicFilesView.setItems(controller.getMusicFiles());
        musicFilesView.setCellFactory(TextFieldListCell.forListView(new FileNameStringConverter()));
        musicFilesView.setPlaceholder(new Region());
        prevTrackBtn.disableProperty().bind(Bindings.not(controller.canGotoPreviousTrack()));
        playPauseBtn.disableProperty().bind(Bindings.not(controller.canTogglePlayPause()));
        nextTractBtn.disableProperty().bind(Bindings.not(controller.canGotoNextTract()));
        // playPauseBtn.selectedProperty().bind(controller.isPlaying());
        controller.isPlayingProperty().bind(playPauseBtn.selectedProperty());
        durationSlider.disableProperty().bind(Bindings.not(controller.isLoaded()));
        durationSlider.valueProperty().bindBidirectional(controller.currentDurationProperty());
        currentTimeLbl.visibleProperty().bind(controller.isLoaded());
        currentTimeLbl.managedProperty().bind(controller.isLoaded());
        currentTimeLbl.textProperty().bind(Bindings.format("%1$tM:%1$tS", controller.currentTimeProperty()));
        totalTimeLbl.visibleProperty().bind(controller.isLoaded());
        totalTimeLbl.managedProperty().bind(controller.isLoaded());
        totalTimeLbl.textProperty().bind(Bindings.format("%1$tM:%1$tS", controller.totalTimeProperty()));
        muteToggleBtn.selectedProperty().bindBidirectional(controller.muteProperty());
        volumeSlider.valueProperty().bindBidirectional(controller.volumeProperty());
        volumeSlider.disableProperty().bind(muteToggleBtn.selectedProperty());
        coverView.imageProperty().bind(controller.coverImageProperty());
        BorderPane coverParent = (BorderPane) coverView.getParent();
        coverView.fitWidthProperty().bind(coverParent.widthProperty());
        coverView.fitHeightProperty().bind(coverParent.heightProperty());
        Circle circleClip = new Circle();
        // coverParent.setClip(circleClip);
        circleClip.radiusProperty().bind(Bindings.min(coverParent.widthProperty().divide(2), coverParent.heightProperty().divide(2)));
        circleClip.centerXProperty().bind(coverParent.layoutXProperty().add(coverParent.widthProperty().divide(2)));
        circleClip.centerYProperty().bind(coverParent.layoutYProperty().add(coverParent.heightProperty().divide(2)));
        coverView.imageProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Color color = controller.getPredominantColor(newValue);
                // color = new Color(1.0 - color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue(), 1.0 - color.getOpacity());
                String colorStr = Util.colorToCssColor(color);
                try {
                    File tmpStyleSheet = File.createTempFile("musicplayer-", "-style.css");
                    tmpStyleSheet.deleteOnExit();
                    URL styleSheet = ClassLoader.getSystemResource("css/default.css");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(styleSheet.openStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpStyleSheet)));
                    reader.lines().forEach(line -> {
                        try {
                            String pattern = "(\\s*)(-fx-base\\s*:\\s*)(#\\w+);";
                            if (line.matches(pattern))
                                line = line.replaceAll(pattern, "$1$2" + colorStr + ";");
                            writer.append(line);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    reader.close();
                    writer.close();
                    stage.getScene().getStylesheets().remove(styleSheet.toExternalForm());
                    stage.getScene().getStylesheets().add(tmpStyleSheet.toURI().toURL().toExternalForm());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // or try to set the cover image as root background image.

        restoreState();
        stage.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || !newValue) {
                saveState();
            }
        });
    }

    private String toHexString(Color color) {
        int colorValue = ((Double.valueOf(color.getOpacity() * 255.0).intValue() & 0xFF) << 24) |
                ((Double.valueOf(color.getRed() * 255.0).intValue() & 0xFF) << 16) |
                ((Double.valueOf(color.getGreen() * 255.0).intValue() & 0xFF) << 8) |
                ((Double.valueOf(color.getBlue() * 255.0).intValue() & 0xFF) << 0);
        String colorStr = "#" + Integer.toHexString(colorValue);
        return colorStr;
    }

    @FXML
    void toggleFullScreen() {
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML
    void onDragDetected(MouseEvent event) {

    }

    @FXML
    void onDragDone(DragEvent event) {

    }

    @FXML
    void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            List<File> files = new ArrayList<>(db.getFiles());
            controller.addFiles(files);
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    void onDragEntered(DragEvent event) {

    }

    @FXML
    void onDragExited(DragEvent event) {

    }

    @FXML
    void onDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles())
            event.acceptTransferModes(TransferMode.COPY);
        event.consume();
    }

    @FXML
    void startDragWindow(MouseEvent event) {
        mouseDragOffsetX = event.getSceneX();
        mouseDragOffsetY = event.getSceneY();
        event.consume();
    }

    @FXML
    void moveWindow(MouseEvent event) {
        stage.setX(event.getScreenX() - mouseDragOffsetX);
        stage.setY(event.getScreenY() - mouseDragOffsetY);
        event.consume();
    }

    @FXML
    void hideWindow(ActionEvent event) {
        stage.setIconified(true);
    }

    @FXML
    void quitApplication(ActionEvent event) {
        stage.close();
        Platform.exit();
    }

    @FXML
    void playNextTrack(ActionEvent event) {
        controller.gotoTrack(+1);
    }

    @FXML
    void playPrevTrack(ActionEvent event) {
        controller.gotoTrack(-1);
    }

    @FXML
    void togglePlayPause(ActionEvent event) {
        controller.togglePlayPause();
    }

    @FXML
    void toggleRepeat(ActionEvent event) {
        controller.toggleRepeat();
    }

    private void initPreferences() {
        if (appPreferences == null) {
            Preferences prefs = Preferences.userRoot();
            appPreferences = prefs.node("olympe/musicplayer");
        }
        if (viewPreferences == null) {
            viewPreferences = appPreferences.node("view");
        }

        if (playerPreferences == null) {
            playerPreferences = appPreferences.node("player");
        }
    }

    private void saveState() {
        viewPreferences.putBoolean("isFullScreen", stage.isFullScreen());
        viewPreferences.putBoolean("isIconified", stage.isIconified());
        viewPreferences.putBoolean("isMaximized", stage.isMaximized());
        viewPreferences.putDouble("xPosition", stage.getX());
        viewPreferences.putDouble("yPosition", stage.getY());
        viewPreferences.putDouble("width", stage.getWidth());
        viewPreferences.putDouble("height", stage.getHeight());

        playerPreferences.putBoolean("isMuteSelected", muteToggleBtn.isSelected());
        playerPreferences.putBoolean("isRepeatSelected", repeatToggleBtn.isSelected());
        playerPreferences.putBoolean("isRepeatIndeterminate", repeatToggleBtn.isIndeterminate());
        playerPreferences.putDouble("volume", volumeSlider.getValue());
        Stream<File> files = musicFilesView.getItems().stream();
        String data = String.join(File.pathSeparator, files.map(File::getAbsolutePath).collect(Collectors.toList()));
        playerPreferences.putByteArray("last-opened", data.getBytes(Charset.defaultCharset()));
    }

    private void restoreState() {
        stage.setFullScreen(viewPreferences.getBoolean("isFullScreen", false));
        stage.setIconified(viewPreferences.getBoolean("isIconified", false));
        stage.setMaximized(viewPreferences.getBoolean("isMaximized", false));
        stage.setX(viewPreferences.getDouble("xPosition", 0));
        stage.setY(viewPreferences.getDouble("yPosition", 0));
        stage.setWidth(viewPreferences.getDouble("width", 600));
        stage.setHeight(viewPreferences.getDouble("height", 480));

        muteToggleBtn.setSelected(playerPreferences.getBoolean("isMuteSelected", false));
        repeatToggleBtn.setSelected(playerPreferences.getBoolean("isRepeatSelected", false));
        repeatToggleBtn.setIndeterminate(playerPreferences.getBoolean("isRepeatIndeterminate", false));
        volumeSlider.setValue(playerPreferences.getDouble("volume", 0.5));
        byte[] bytes = playerPreferences.getByteArray("last-opened", null);
        if (bytes != null)
        {
            String data = new String(bytes, Charset.defaultCharset());
            String[] filePathnames = data.split(File.pathSeparator);
            Stream<File> files = Stream.of(filePathnames).map(File::new);
            Platform.runLater(() -> musicFilesView.getItems().addAll(files.collect(Collectors.toList())));
        }
    }
}

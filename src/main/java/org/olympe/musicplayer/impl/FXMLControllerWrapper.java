package org.olympe.musicplayer.impl;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verifier;
import com.github.scribejava.core.oauth.OAuthService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import jfxtras.labs.util.Util;
import org.jaudiotagger.tag.TagField;
import org.olympe.musicplayer.MusicPlayerApplication;
import org.olympe.musicplayer.MusicPlayerController;
import org.olympe.musicplayer.genius.Genius;
import org.olympe.musicplayer.genius.GeniusApi;
import org.olympe.musicplayer.impl.control.AudioListCell;
import org.olympe.musicplayer.impl.control.TagTableCell;
import org.olympe.musicplayer.impl.util.FileNameStringConverter;
import org.olympe.musicplayer.impl.util.MusicFileTag;
import org.olympe.musicplayer.impl.util.TagFileNameStringConverter;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FXMLControllerWrapper {

    private final MusicPlayerController controller;
    private final Stage stage;
    private final MusicPlayerApplication musicPlayerApplication;

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
    @FXML
    private Button removeMusicFilesBtn;
    @FXML
    private TableView<TagField> tagsView;
    @FXML
    private ResourceBundle resources;
    @FXML
    private BorderPane dialogPane;
    @FXML
    private ButtonBar dialogButtonBar;
    @FXML
    private HBox dialogHeaderPane;
    @FXML
    private Label dialogHeaderLabel;

    private FileChooser fileChooser;
    private ApplicationNotifierImpl notifier;

    private Preferences appPreferences;
    private Preferences viewPreferences;
    private Preferences playerPreferences;

    private Genius genius;

    public FXMLControllerWrapper(MusicPlayerController controller, Stage primaryStage, MusicPlayerApplication musicPlayerApplication) {
        this.controller = controller;
        this.stage = primaryStage;
        this.musicPlayerApplication = musicPlayerApplication;
        initPreferences();
    }

    @FXML
    void initialize() {
        notifier = new ApplicationNotifierImpl();
        notifier.overlayPane = overlayPane;
        notifier.dialogPane = dialogPane;
        notifier.dialogButtonBar = dialogButtonBar;
        notifier.dialogHeaderPane = dialogHeaderPane;
        notifier.dialogHeaderLabel = dialogHeaderLabel;
        musicFilesView.setItems(controller.getMusicFiles());
        musicFilesView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        musicFilesView.setCellFactory(AudioListCell.forListView(controller, new FileNameStringConverter()));
        musicFilesView.setPlaceholder(new Region());
        removeMusicFilesBtn.visibleProperty().bind(Bindings.isNotEmpty(musicFilesView.getSelectionModel().getSelectedItems()));
        removeMusicFilesBtn.managedProperty().bind(Bindings.isNotEmpty(musicFilesView.getSelectionModel().getSelectedItems()));
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
        TableColumn<TagField, String> column = (TableColumn<TagField, String>) tagsView.getColumns().get(0);
        column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getId()));
        column.setCellFactory(TagTableCell.forTableColumn(new TagFileNameStringConverter(resources)));
        column = (TableColumn<TagField, String>) tagsView.getColumns().get(1);
        column.setCellFactory(TagTableCell.forTableColumn(new DefaultStringConverter()));
        column.setCellValueFactory(param -> {
            TagField field = param.getValue();
            MusicFileTag tag = controller.musicFileTagProperty().get();
            return new ReadOnlyStringWrapper(tag.getTagValue(field));
        });
        controller.musicFileTagProperty().addListener((observable1, oldValue1, newValue1) -> {
            if (newValue1 != null) {
                tagsView.setItems(FXCollections.observableArrayList(newValue1.getTagFields()));
                if (false && genius == null)
                {
                    String secretState = "secret" + new Random().nextInt(999_999);
                    String apiKey = "Z6rpk9Dor60GpN_r-0z1jiPg2AIhpG7e7R2IM5Lv5gjA6Qj8BG44I2kulZkMfNgY";
                    String apiSecret = "_S8D-nB84IyJ9nb7FdSvmDIBNF-YN6AO46eAQozyCb6VWEeJCKwUImfwwJgONuYzkE4LOA8dGiN3ulcransiNQ";
                    OAuthService service = new ServiceBuilder().apiKey(apiKey)
                            .apiSecret(apiSecret)
                            .state(secretState)
                            .scope("me")
                            .provider(GeniusApi.class)
                            .callback("http://localhost:8080/ws/oauth")
                            .build();
                    Token reqToken = null;
                    String url = service.getAuthorizationUrl(reqToken);
                    musicPlayerApplication.getHostServices().showDocument(url);
                    String value = notifier.askString("Enter verifier: ");
                    if (value == null)
                        return;
                    Verifier verifier = new Verifier(value);
                    try
                    {
                        Token accessToken = service.getAccessToken(reqToken, verifier);
                        System.out.println(accessToken);
                        genius = new Genius();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
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
        Platform.runLater(this::initializeAccelerators);
    }

    private void initializeAccelerators() {
        Scene scene = stage.getScene();
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DELETE), fire(removeMusicFilesBtn));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.PLAY), fire(playPauseBtn));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.PAUSE), fire(playPauseBtn));
    }

    private Runnable fire(ButtonBase btn) {
        return () -> Platform.runLater(btn::fire);
    }

    /*private String toHexString(Color color) {
        int colorValue = ((Double.valueOf(color.getOpacity() * 255.0).intValue() & 0xFF) << 24) |
                ((Double.valueOf(color.getRed() * 255.0).intValue() & 0xFF) << 16) |
                ((Double.valueOf(color.getGreen() * 255.0).intValue() & 0xFF) << 8) |
                ((Double.valueOf(color.getBlue() * 255.0).intValue() & 0xFF));
        return "#" + Integer.toHexString(colorValue);
    }*/

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

    @FXML
    void addMusicFiles() {
        if (fileChooser == null) {
            fileChooser = new FileChooser();
            String pathName = viewPreferences.get("lastInitialDirectory", System.getProperty("user.home"));
            fileChooser.setInitialDirectory(new File(pathName));
            fileChooser.setTitle("Choose audio files to play...");

            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac"));
        }
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        if (files != null && !files.isEmpty()) {
            File dir = files.get(0).getParentFile();
            fileChooser.setInitialDirectory(dir);
            controller.addFiles(files);
        }
    }

    @FXML
    void removeMusicFiles(ActionEvent event) {
        if (event != null && event.getSource() == removeMusicFilesBtn) {
            List<File> selecteds = musicFilesView.getSelectionModel().getSelectedItems();
            if (selecteds != null && !selecteds.isEmpty()) {
                musicFilesView.getItems().removeAll(selecteds);
                musicFilesView.getSelectionModel().clearSelection();
            }
            event.consume();
        }
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
        if (fileChooser != null) {
            File dir = fileChooser.getInitialDirectory();
            if (dir != null)
                viewPreferences.put("lastInitialDirectory", dir.getAbsolutePath());
        }

        playerPreferences.putBoolean("isMuteSelected", muteToggleBtn.isSelected());
        playerPreferences.putBoolean("isRepeatSelected", repeatToggleBtn.isSelected());
        playerPreferences.putBoolean("isRepeatIndeterminate", repeatToggleBtn.isIndeterminate());
        playerPreferences.putDouble("volume", volumeSlider.getValue());
        Stream<File> files = musicFilesView.getItems().stream();
        String data = String.join(File.pathSeparator, files.map(File::getAbsolutePath).collect(Collectors.toList()));
        playerPreferences.putByteArray("last-opened", data.getBytes(Charset.defaultCharset()));
        playerPreferences.putInt("last-played", controller.currentIndexProperty().get());
        playerPreferences.putDouble("last-time", controller.currentDurationProperty().get());
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
        if (bytes != null) {
            String data = new String(bytes, Charset.defaultCharset());
            String[] filePathnames = data.split(File.pathSeparator);
            Stream<File> files = Stream.of(filePathnames).map(File::new);
            Platform.runLater(() -> {
                musicFilesView.getItems().addAll(files.collect(Collectors.toList()));
                controller.gotoTrack(playerPreferences.getInt("last-played", 0));
                Platform.runLater(() -> durationSlider.setValue(playerPreferences.getDouble("last-time", 0)));
            });
        }
    }
}

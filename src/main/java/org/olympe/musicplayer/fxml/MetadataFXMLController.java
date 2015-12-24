package org.olympe.musicplayer.fxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import org.olympe.musicplayer.bean.model.Audio;

/**
 *
 */
public abstract class MetadataFXMLController extends AbstractExtraFXMLController
{
    @FXML
    private TableView<AudioTag> tagsView;
    @FXML
    private TableColumn<AudioTag, String> tagKeyColumn;
    @FXML
    private TableColumn<AudioTag, String> tagValueColumn;
    private Map<Audio, ObservableList<AudioTag>> tagsCache;
    private List<FieldKey> supportedTags = new ArrayList<>();

    public MetadataFXMLController(Application application, Stage stage)
    {
        super(application, stage);
        tagsCache = new HashMap<>();
        registerSupportedTags(supportedTags);
    }

    public void registerSupportedTags(List<FieldKey> supportedTags)
    {
        supportedTags.add(FieldKey.ALBUM);
        supportedTags.add(FieldKey.ALBUM_ARTIST);
        supportedTags.add(FieldKey.ARTIST);
        supportedTags.add(FieldKey.TITLE);
        supportedTags.add(FieldKey.COMPOSER);
        supportedTags.add(FieldKey.GENRE);
        supportedTags.add(FieldKey.YEAR);
    }

    @Override
    protected void updateAudio(ObservableValue<? extends Audio> observable, Audio oldValue, Audio newValue)
    {
        super.updateAudio(observable, oldValue, newValue);
        tagsView.setItems(tagsCache.get(newValue));
    }

    @Override
    protected void registerAudio(Audio audio)
    {
        super.registerAudio(audio);
        ObservableList<AudioTag> tags = FXCollections.observableArrayList();
        Tag tag = audio.getTag();
        for (FieldKey fieldKey : supportedTags)
        {
            String fieldValue = tag.getFirst(fieldKey);
            if (fieldValue != null && !fieldValue.isEmpty())
                tags.add(new AudioTag(fieldKey, fieldValue));
        }
        tagsCache.put(audio, tags);
    }

    @Override
    protected void unregisterAudio(Audio audio)
    {
        super.unregisterAudio(audio);
        tagsCache.remove(audio).clear();
    }

    @Override
    void initialize()
    {
        super.initialize();
        StackPane placeholder1 = new StackPane();
        StackPane placeholder2 = new StackPane();
        placeholder1.getChildren().add(new Label(localize("MetadataView.PlaceHolder.noAudioMsg")));
        placeholder2.getChildren().add(new Label(localize("MetadataView.PlaceHolder.emptyMsg")));
        tagsView.placeholderProperty().bind(Bindings.when(loadedAudioProperty().isNull()).then(placeholder1).otherwise(placeholder2));
        tagKeyColumn.setCellValueFactory(new PropertyValueFactory<>("fieldKey"));
        tagKeyColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        tagValueColumn.setCellValueFactory(new PropertyValueFactory<>("fieldValue"));
        tagValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        tagsView.getSelectionModel().selectedItemProperty().addListener(observable -> {
            Platform.runLater(() -> tagsView.getSelectionModel().clearSelection());
        });
    }

    public static class AudioTag
    {
        private StringProperty fieldKey;
        private StringProperty fieldValue;

        AudioTag(FieldKey fieldKey, String fieldValue)
        {
            this.fieldKey = new SimpleStringProperty(this, "fieldKey", localize("Metadata." + fieldKey.name()));
            this.fieldValue = new SimpleStringProperty(this, "fieldValue", fieldValue);
        }

        public StringProperty fieldKeyProperty()
        {
            return fieldKey;
        }

        public String getFieldKey()
        {
            return fieldKey.get();
        }

        public void setFieldKey(String fieldKey)
        {
            this.fieldKey.set(fieldKey);
        }

        public StringProperty fieldValueProperty()
        {
            return fieldValue;
        }

        public String getFieldValue()
        {
            return fieldValue.get();
        }

        public void setFieldValue(String fieldValue)
        {
            this.fieldValue.set(fieldValue);
        }
    }
}

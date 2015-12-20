package org.olympe.musicplayer.fxml;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import org.olympe.musicplayer.bean.model.Audio;

/**
 *
 */
public abstract class CoverImageFXMLController extends MusicPlayerFXMLController
{
    @FXML
    private ImageView coverView;
    private ObservableMap<Object, Image> imageCache;

    public CoverImageFXMLController(Application application, Stage stage)
    {
        super(application, stage);
        imageCache = FXCollections.observableHashMap();
    }

    @Override
    protected void updateAudio(ObservableValue<? extends Audio> observable, Audio oldValue, Audio newValue)
    {
        super.updateAudio(observable, oldValue, newValue);
        coverView.setImage(null);
        if (newValue != null)
        {
            Tag tag = newValue.getTag();
            Artwork artwork = tag.getFirstArtwork();
            Image image = getImage(artwork);
            if (image != null)
                coverView.setImage(image);
        }
    }

    private Image getImage(Artwork artwork)
    {
        Image image = null;
        if (artwork != null)
        {
            String url = artwork.getImageUrl();
            byte[] data = artwork.getBinaryData();
            Object source = (url != null) ? url : ((data != null) ? ByteBuffer.wrap(data) : null);
            if (source != null)
            {
                image = imageCache.get(source);
                if (image == null)
                {
                    if (source == url)
                    {
                        image = new Image(url);
                    }
                    else
                    {
                        image = new Image(new ByteArrayInputStream(data));
                    }
                    imageCache.put(source, image);
                }
            }
        }
        return image;
    }
}

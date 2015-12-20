package org.olympe.musicplayer.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

import org.apache.tika.Tika;
import org.kohsuke.MetaInfServices;

@MetaInfServices(FileTypeDetector.class)
public class TikaFileTypeDetector extends FileTypeDetector
{
    private Tika tika = new Tika();

    @Override
    public String probeContentType(Path path) throws IOException
    {
        if (path == null)
            return null;
        return tika.detect(path.toFile());
    }
}

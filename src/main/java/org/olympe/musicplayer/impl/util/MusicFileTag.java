package org.olympe.musicplayer.impl.util;

import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MusicFileTag {

    private final Tag tag;
    private List<TagField> fields;

    public MusicFileTag(Tag tag) {
        this.tag = tag;
    }

    public Collection<TagField> getTagFields() {
        if (fields == null) {
            fields = new ArrayList<>();
            tag.getFields().forEachRemaining(fields::add);
            List<TagField> toRemove = fields.parallelStream().filter(TagField::isEmpty).collect(Collectors.toList());
            if (toRemove != null)
                fields.removeAll(toRemove);
            toRemove = fields.parallelStream().filter(TagField::isBinary).collect(Collectors.toList());
            if (toRemove != null)
                fields.removeAll(toRemove);
        }
        return fields;
    }

    public String getTagValue(TagField field) {
        return tag.getFirst(field.getId());
    }
}

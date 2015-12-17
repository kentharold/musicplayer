package org.olympe.musicplayer.impl.util;

import javafx.scene.Node;

public interface ApplicationNotifier {

    void inform(String msg);

    void warn(String msg);

    boolean ask(String question);

    String askString(String prompt);

    void show(String title, Node content, Node graphic);
}

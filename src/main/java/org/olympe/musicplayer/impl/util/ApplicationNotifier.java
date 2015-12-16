package org.olympe.musicplayer.impl.util;

public interface ApplicationNotifier {

    void inform(String msg);

    void warn(String msg);

    boolean ask(String question);

    String askString(String prompt);
}

package hci.interfaces;

import java.io.File;

public interface UserInputModel {
    String getUserInput();

    boolean addUserInput(String s);

    void openFile(File file);
}

package hci.interfaces;

import hci.InputModel;
import hci.InputView;

import java.io.File;

public interface UserInputPresenter {
    UserInputModel getModel();

    void setModel(InputModel inputModel);

    void handleUserInput(String input);

    void run();

    void handleFile(File file);
}

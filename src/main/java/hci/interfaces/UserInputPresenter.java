package hci.interfaces;

import cora.exceptions.ParserException;
import hci.InputModel;
import hci.InputView;

import java.io.File;

public interface UserInputPresenter {
    UserInputModel getModel();

    void setModel(InputModel inputModel);

    void handleUserInput(String input);

    void run();

    void handleFile(File file);

    void enterProof(String proof) throws ParserException;

    void displayWarning(String ex);

    void saveProof(File file);

    void loadProof(File file);
}

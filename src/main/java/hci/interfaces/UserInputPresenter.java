package hci.interfaces;

import cora.exceptions.ParserException;
import hci.InputModel;

import java.io.File;

public interface UserInputPresenter {
    UserInputModel getModel();

    void setModel(InputModel inputModel);

    void handleUserInput(String input);

    void run();

    void handleFile(File file);

    void enterProof(String proof, String r, String c) throws ParserException;

    void displayWarning(String ex);

    void displayProofFinished(String msg);

    void saveProof(File file);

    void loadProof(File file);

    void changeFontSize(float size);
}

package hci.interfaces;

import cora.exceptions.ParserException;

import java.io.File;

public interface UserInputModel {
    String getUserInput();

    String getPreviousInput();

    String getNextInput();

    boolean addUserInput(String s);

    void openFile(File file);

    void enterProof(String proof) throws ParserException;
    
    String getRules();

    String getEquations();
}

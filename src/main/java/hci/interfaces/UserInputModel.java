package hci.interfaces;

import cora.exceptions.ParserException;
import hci.InputPresenter;

import java.io.File;

public interface UserInputModel {

    public InputPresenter getPresenter();

    public void setPresenter(InputPresenter inputPresenter);
    String getUserInput();

    String getPreviousInput();

    String getNextInput();

    boolean addUserInput(String s);

    void openFile(File file);

    void enterProof(String proof) throws ParserException;
    
    String getRules();

    String getEquations();

    boolean getCompleteness();

    boolean getBottom();
}

package hci.interfaces;

import cora.exceptions.ParserException;
import hci.InputPresenter;

import java.io.File;
import java.io.IOException;

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

    String getRulesLeft();

    String getRulesRight();

    String getRulesConstraint();

    String getEquations();

    String getEquationsLeft();

    String getEquationsRight();

    String getEquationsConstraint();

    boolean getCompleteness();

    boolean getBottom();

    void saveProofToFile(File file);

    void loadProofFromFile(File file) throws ParserException;
}

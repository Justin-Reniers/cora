package cora.interfaces.smt;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;

import java.io.IOException;

public interface Proof {
    public void saveStateToFile(String filePath) throws IOException;

    public UserCommand getLastCommand();

    public TRS getLcTrs();

    public Term getLeft();

    public Term getRight();

    public String toString();

    public String currentState();

    public boolean applyNewUserCommand(String uCommand);
}

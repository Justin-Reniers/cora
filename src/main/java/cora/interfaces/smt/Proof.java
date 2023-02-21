package cora.interfaces.smt;

import cora.interfaces.rewriting.TRS;
import cora.interfaces.terms.Term;

public interface Proof {
    public void writeToFile(String filePath);

    public TRS getLcTrs();

    public Term getLeft();

    public Term getRight();

    public String toString();
}

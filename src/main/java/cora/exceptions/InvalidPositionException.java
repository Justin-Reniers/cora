package cora.exceptions;

import com.ibm.icu.text.Transliterator;
import cora.interfaces.terms.Position;

import java.util.ArrayList;

public class InvalidPositionException extends Error {
    public InvalidPositionException(String pos) {
        super("Command was called with invalid position " + pos);
    }
}

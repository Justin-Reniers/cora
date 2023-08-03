package cora.exceptions;

import java.util.ArrayList;

public class InvalidArgumentException extends Error {
    public InvalidArgumentException(String command, ArrayList<String> arguments) {
        super("Command " + command + " was called with invalid arguments " + arguments);
    }
}

package cora.exceptions;

/**
 * A BottomException is thrown when an EquivalenceProof has no equations and its bottom variable is set to true,
 * but another UserCommand is trying to be applied.
 */
public class BottomException extends Error {
    public BottomException(String userCommand) {
        super("Tried to apply " + userCommand + " to proof when value of bottom is already true");
    }
}

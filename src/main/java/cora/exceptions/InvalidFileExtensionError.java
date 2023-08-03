package cora.exceptions;

public class InvalidFileExtensionError extends Error {
    public InvalidFileExtensionError(String extension) {
        super("File with extension ." + extension + " cannot be opened");
    }
}

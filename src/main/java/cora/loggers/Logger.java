package cora.loggers;

import cora.interfaces.logging.ILogger;

public class Logger {

    private static ILogger il;

    public Logger(ILogger il) {
        Logger.il = il;
    }

    public static void log(String s) {
        il.log(s);
    }

    public static void finalized() {
        il.finalized();
    }

}

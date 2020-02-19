package cora.provingstrategies;

import java.util.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Logging {
    static Logger logger;
    static Handler fileHandler;
    Formatter plainText;

    private static Logger getLogger() {
        if (logger == null) {
            new Logging();
        }
        return logger;
    }

    public static void log(Level level, String msg) {
        getLogger().log(level, msg);
        System.out.println(msg);
    }
}

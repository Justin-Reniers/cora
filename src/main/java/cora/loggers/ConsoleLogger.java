package cora.loggers;

import cora.interfaces.logging.ILogger;

public class ConsoleLogger implements ILogger {

    @Override
    public void log(String s) {
        System.out.println(s);
    }

    @Override
    public void finalized() {}
}

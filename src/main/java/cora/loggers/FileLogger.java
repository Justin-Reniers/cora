package cora.loggers;

import cora.interfaces.logging.ILogger;
import java.io.FileWriter;
import java.io.IOException;

public class FileLogger implements ILogger {

    private String log;
    private String path;

    public FileLogger(String path) {
        this.path = path;
        this.log = "";
    }

    @Override
    public void log(String s) {
        log = log.concat(s).concat("\n");
    }

    @Override
    public void finalized() {
        try {
            FileWriter fw = new FileWriter(path);
            fw.write(log);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

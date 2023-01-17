package hci;

import hci.interfaces.UserInputModel;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class InputModel implements UserInputModel {
    private final ArrayList<String> userCommands;
    private String lastCommand;

    public InputModel() {
        userCommands = new ArrayList<String>();
    }

    public void initDefault() {

    }

    @Override
    public String getUserInput() {
        return lastCommand;
    }

    @Override
    public boolean addUserInput(String input) {
        if (validUserInput(input)) {
            userCommands.add(input);
            lastCommand = input;
            return true;
        }
        return false;
    }

    @Override
    public void openFile(File file) {
        StringBuilder content = new StringBuilder();
        if (file.canRead() && file.getName().contains(".lctrs")) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(file));
                String line = in.readLine();
                while (line != null) {
                    content.append(line).append("\n");
                    line = in.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(content);
        } else {
            System.out.println("Invalid file");
        }
    }

    private boolean validUserInput(String input) {
        return true;
    }

    private void parseUserInput(String input) {
        //TODO
    }
}

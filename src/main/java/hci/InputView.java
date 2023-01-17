package hci;

import hci.interfaces.UserInputView;

import javax.swing.*;
import java.io.File;

public class InputView extends JFrame implements UserInputView {
    private InputPresenter inputPresenter;
    private JFrame frame;
    private JLabel userInputLabel;
    private JTextField userInput;
    private JMenuBar menuBar;
    private JMenu fileMenu, lcTrsMenu;
    private JMenuItem loadFile;

    public InputView(String title) {
        initComponents(title);
        initEventListeners();
    }

    private void initComponents(String title) {
        initJFrame(title);
        initUserInputBoxes();
        initMenu();
        frame.setJMenuBar(menuBar);
        frame.add(userInput);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initJFrame(String title) {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(500, 200);
    }

    private void initMenu() {
        fileMenu = new JMenu("File");
        loadFile = new JMenuItem("Open");
        fileMenu.add(loadFile);
        //TODO add more File menu options (if necessary)

        lcTrsMenu = new JMenu("LcTrs");
        //TODO add more LcTrs menu options (like state saving)

        menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(lcTrsMenu);
    }

    private void initUserInputBoxes() {
        userInputLabel = new JLabel("Input: ");
        userInputLabel.setBounds(20, 100, 40, 30);
        userInput = new JTextField();
        userInput.setBounds(40, 100, 200, 30);
    }

    private void initEventListeners() {
        userInputEnterActionPerformed();
        loadFileActionPerformed();
    }

    private void userInputEnterActionPerformed() {
        userInput.addActionListener(e -> {
            String input = userInput.getText();
            getPresenter().handleUserInput(input);
        });
    }

    private void loadFileActionPerformed() {
        loadFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION)  {
                File file = fileChooser.getSelectedFile();
                getPresenter().handleFile(file);
            }
        });
    }

    private void clearUserInput() {
        userInput.setText("");
    }

    @Override
    public InputPresenter getPresenter() {
        return inputPresenter;
    }

    @Override
    public void setPresenter(InputPresenter inputPresenter) {
        this.inputPresenter = inputPresenter;
    }

    @Override
    public void onEnterAction() {
        clearUserInput();
    }
}

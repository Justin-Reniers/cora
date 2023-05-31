package hci;

import cora.exceptions.ParserException;
import hci.interfaces.UserInputView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

public class InputView extends JFrame implements UserInputView {
    private InputPresenter inputPresenter;
    private JFrame frame;
    private JLabel userInputLabel, equationLabel, ruleLabel;
    private JTextField userInput;
    private JMenuBar menuBar;
    private JMenu fileMenu, lcTrsMenu;
    private JTextArea equationTextArea, ruleTextArea;
    private JMenuItem loadFile, enterProof;

    public InputView(String title) {
        initComponents(title);
        initEventListeners();
    }

    private void initComponents(String title) {
        initJFrame(title);
        initUserInputBoxes();
        initMenu();
        initTextAreas();
        frame.setLayout(null);
        frame.setVisible(true);
    }

    private void initJFrame(String title) {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(900, 400);
    }

    private void initMenu() {
        fileMenu = new JMenu("File");
        loadFile = new JMenuItem("Open");
        fileMenu.add(loadFile);
        enterProof = new JMenuItem("Proof");
        fileMenu.add(enterProof);
        //TODO add more File menu options (if necessary)

        lcTrsMenu = new JMenu("LcTrs");
        //TODO add more LcTrs menu options (like state saving)

        menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(lcTrsMenu);
        frame.setJMenuBar(menuBar);
    }

    private void initUserInputBoxes() {
        userInputLabel = new JLabel("Input: ");
        userInputLabel.setBounds(5, 250, 40, 30);
        frame.add(userInputLabel);
        userInput = new JTextField();
        userInput.setBounds(40, 250, 200, 30);
        frame.add(userInput);
    }

    private void initTextAreas() {
        Font font = new Font("LucidaSans", Font.PLAIN, 12);

        equationLabel = new JLabel("Equations");
        equationLabel.setBounds(5, 0, 80, 30);
        equationTextArea = new JTextArea("Equations", 100, 60);
        equationTextArea.setFont(font);
        equationTextArea.setEditable(false);
        JScrollPane equationScroll = new JScrollPane(equationTextArea);
        equationScroll.setSize(400, 200);
        equationScroll.setBounds(5, 40, 400, 200);

        ruleLabel = new JLabel("Rules");
        ruleLabel.setBounds(450, 0, 80, 30);
        ruleTextArea = new JTextArea("Rules", 50, 60);
        ruleTextArea.setFont(font);
        ruleTextArea.setEditable(false);
        JScrollPane ruleScroll = new JScrollPane(ruleTextArea);
        ruleScroll.setSize(400, 200);
        ruleScroll.setBounds(450, 40, 400, 200);

        frame.add(equationLabel);
        frame.add(ruleLabel);
        frame.add(equationScroll);
        frame.add(ruleScroll);
    }

    private void initEventListeners() {
        userInputEnterActionPerformed();
        loadFileActionPerformed();
        enterEquivalenceProofActionPerformed();
        upArrowKeyActionPerformed();
        downArrowKeyActionPerformed();
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

    private void enterEquivalenceProofActionPerformed() {
        enterProof.addActionListener(e -> {
            String proof = JOptionPane.showInputDialog(frame, "Enter proof terms and constraint");
            getPresenter().enterProof(proof);
        });
    }

    private void upArrowKeyActionPerformed() {
        userInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) userInput.setText(getPresenter().getModel().getPreviousInput());
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }

    private void downArrowKeyActionPerformed() {
        userInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) userInput.setText(getPresenter().getModel().getNextInput());
            }
            @Override
            public void keyReleased(KeyEvent e) {}
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

    @Override
    public void invalidRuleAction() {
        JOptionPane.showMessageDialog(frame, "Rule cannot be applied to current proof state",
                "Warning", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void updateRulesField(String rules) {
        ruleTextArea.setText(rules);
    }

    @Override
    public void updateEquationsField(String equations) {
        equationTextArea.setText(equations);
    }
}

package hci;

import cora.exceptions.InvalidPositionException;
import cora.exceptions.ParserException;
import hci.interfaces.UserInputView;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
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
    private JTextPane equationTextArea, ruleTextArea;
    private JMenuItem loadFile, enterProof;
    private JCheckBox _bottom, _completeness;

    public InputView(String title) {
        initComponents(title);
        initEventListeners();
    }

    private void initComponents(String title) {
        initJFrame(title);
        initUserInputBoxes();
        initMenu();
        initTextAreas();
        initCheckBoxes();
        frame.setLayout(null);
        frame.setVisible(true);
    }

    private void initJFrame(String title) {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //frame.setSize(1500, 800);
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
        userInputLabel.setBounds(5, 720, 40, 30);
        frame.add(userInputLabel);
        userInput = new JTextField();
        userInput.setBounds(5, 750, 1100, 30);
        frame.add(userInput);
    }

    private void initTextAreas() {
        Font font = new Font("LucidaSans", Font.PLAIN, 12);

        ruleLabel = new JLabel("Rules");
        ruleLabel.setBounds(5, 0, 80, 30);
        ruleTextArea = new JTextPane();
        ruleTextArea.setContentType("text/html;charset=UTF-8");
        ruleTextArea.setFont(font);
        ruleTextArea.setEditable(false);
        JScrollPane ruleScroll = new JScrollPane(ruleTextArea);
        ruleScroll.setSize(1100, 300);
        ruleScroll.setBounds(5, 30, 1100, 300);

        equationLabel = new JLabel("Equations");
        equationLabel.setBounds(5, 375, 80, 30);
        equationTextArea = new JTextPane();
        equationTextArea.setContentType("text/html;charset=UTF-8");
        equationTextArea.setFont(font);
        equationTextArea.setEditable(false);
        JScrollPane equationScroll = new JScrollPane(equationTextArea);
        equationScroll.setSize(1100, 300);
        equationScroll.setBounds(5, 405, 1100, 300);

        frame.add(ruleLabel);
        frame.add(equationLabel);
        frame.add(ruleScroll);
        frame.add(equationScroll);
    }

    private void initCheckBoxes() {
        _bottom = new JCheckBox();
        _completeness = new JCheckBox();
        _bottom.setEnabled(false);
        _completeness.setEnabled(false);
        JLabel bottomLabel = new JLabel("Bottom: ");
        JLabel completenessLabel = new JLabel("Completeness: ");
        frame.add(_bottom);
        frame.add(_completeness);
        _bottom.setBounds(1300, 5, 20, 20);
        _completeness.setBounds(1300, 35, 20, 20);
        frame.add(bottomLabel);
        frame.add(completenessLabel);
        bottomLabel.setBounds(1200, 5, 100, 20);
        completenessLabel.setBounds(1200, 35, 100, 20);
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
            updateBottomField(getPresenter().getModel().getBottom());
            updateCompletenessField(getPresenter().getModel().getCompleteness());
        });
    }

    private void upArrowKeyActionPerformed() {
        userInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    getPresenter().getModel().addUserInput(userInput.getText());
                    userInput.setText(getPresenter().getModel().getPreviousInput());
                }
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
    public void warningDialog(String ex) {
        JOptionPane.showMessageDialog(frame, ex, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void updateRulesField(String rules) {
        ruleTextArea.setText(rules);
    }

    @Override
    public void updateEquationsField(String equations) {
        equationTextArea.setText(equations);
    }

    @Override
    public void updateCompletenessField(boolean completeness) {
        _completeness.setSelected(completeness);
    }

    @Override
    public void updateBottomField(boolean bottom) {
        _bottom.setSelected(bottom);
    }
}

package hci;

import hci.interfaces.UserInputView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

public class InputView extends JFrame implements UserInputView {
    private InputPresenter inputPresenter;
    private JFrame frame;
    private JLabel userInputLabel, equationLabel, ruleLabel, positionsLabel;
    private JTextField userInput;
    private JMenuBar menuBar;
    private JMenu fileMenu, lcTrsMenu;
    private JTextPane ruleTextAreaLeft, ruleTextAreaRight, ruleTextAreaConstraint;
    private JTextPane equationTextAreaLeft, equationTextAreaRight, equationTextAreaConstraint;
    private JTextPane positionTextArea;
    private JMenuItem loadFile, enterProof, saveProof, loadProof;
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
        saveProof = new JMenuItem("Save proof");
        loadProof = new JMenuItem("Load proof");
        lcTrsMenu.add(saveProof);
        lcTrsMenu.add(loadProof);
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
        ruleTextAreaLeft = new JTextPane();
        ruleTextAreaLeft.setContentType("text/html;charset=UTF-8");
        ruleTextAreaLeft.setFont(font);
        ruleTextAreaLeft.setEditable(false);
        ruleTextAreaRight = new JTextPane();
        ruleTextAreaRight.setContentType("text/html;charset=UTF-8");
        ruleTextAreaRight.setFont(font);
        ruleTextAreaRight.setEditable(false);
        ruleTextAreaConstraint = new JTextPane();
        ruleTextAreaConstraint.setContentType("text/html;charset=UTF-8");
        ruleTextAreaConstraint.setFont(font);
        ruleTextAreaConstraint.setEditable(false);
        JScrollPane ruleScroll1 = new JScrollPane(ruleTextAreaLeft);
        ruleScroll1.setSize(400, 300);
        ruleScroll1.setBounds(5, 30, 400, 300);
        JScrollPane ruleScroll2 = new JScrollPane(ruleTextAreaRight);
        ruleScroll2.setSize(400, 300);
        ruleScroll2.setBounds(405, 30, 400, 300);
        JScrollPane ruleScroll3 = new JScrollPane(ruleTextAreaConstraint);
        ruleScroll3.setSize(400, 300);
        ruleScroll3.setBounds(805, 30, 400, 300);
        Synchronizer sync = new Synchronizer(ruleScroll1, ruleScroll2, ruleScroll3);
        ruleScroll1.getVerticalScrollBar().addAdjustmentListener(sync);
        ruleScroll1.getHorizontalScrollBar().addAdjustmentListener(sync);
        ruleScroll2.getVerticalScrollBar().addAdjustmentListener(sync);
        ruleScroll2.getHorizontalScrollBar().addAdjustmentListener(sync);
        ruleScroll3.getVerticalScrollBar().addAdjustmentListener(sync);
        ruleScroll3.getHorizontalScrollBar().addAdjustmentListener(sync);

        equationLabel = new JLabel("Equations");
        equationLabel.setBounds(5, 375, 80, 30);

        equationTextAreaLeft = new JTextPane();
        equationTextAreaLeft.setContentType("text/html;charset=UTF-8");
        equationTextAreaLeft.setFont(font);
        equationTextAreaLeft.setEditable(false);
        equationTextAreaRight = new JTextPane();
        equationTextAreaRight.setContentType("text/html;charset=UTF-8");
        equationTextAreaRight.setFont(font);
        equationTextAreaRight.setEditable(false);
        equationTextAreaConstraint = new JTextPane();
        equationTextAreaConstraint.setContentType("text/html;charset=UTF-8");
        equationTextAreaConstraint.setFont(font);
        equationTextAreaConstraint.setEditable(false);
        JScrollPane equationScroll1 = new JScrollPane(equationTextAreaLeft);
        equationScroll1.setSize(400, 300);
        equationScroll1.setBounds(5, 405, 300, 300);
        JScrollPane equationScroll2 = new JScrollPane(equationTextAreaRight);
        equationScroll2.setSize(300, 300);
        equationScroll2.setBounds(305, 405, 300, 300);
        JScrollPane equationScroll3 = new JScrollPane(equationTextAreaConstraint);
        equationScroll3.setSize(300, 300);
        equationScroll3.setBounds(605, 405, 300, 300);
        Synchronizer sync2 = new Synchronizer(ruleScroll1, ruleScroll2, ruleScroll3);
        equationScroll1.getVerticalScrollBar().addAdjustmentListener(sync);
        equationScroll1.getHorizontalScrollBar().addAdjustmentListener(sync);
        equationScroll2.getVerticalScrollBar().addAdjustmentListener(sync);
        equationScroll2.getHorizontalScrollBar().addAdjustmentListener(sync);
        equationScroll3.getVerticalScrollBar().addAdjustmentListener(sync);
        equationScroll3.getHorizontalScrollBar().addAdjustmentListener(sync);

        frame.add(ruleLabel);
        frame.add(equationLabel);
        frame.add(ruleScroll1);
        frame.add(ruleScroll2);
        frame.add(ruleScroll3);
        frame.add(equationScroll1);
        frame.add(equationScroll2);
        frame.add(equationScroll3);

        positionsLabel = new JLabel("Positions and sub-terms");
        positionsLabel.setBounds(950, 375, 80, 30);

        positionTextArea = new JTextPane();
        positionTextArea.setContentType("text/html;charset=UTF-8");
        positionTextArea.setFont(font);
        positionTextArea.setEditable(false);
        JScrollPane positionScroll = new JScrollPane(positionTextArea);
        positionScroll.setSize(300, 300);
        positionScroll.setBounds(950, 405, 300, 300);

        frame.add(positionsLabel);
        frame.add(positionScroll);
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
        _bottom.setBounds(1350, 5, 20, 20);
        _completeness.setBounds(1350, 35, 20, 20);
        frame.add(bottomLabel);
        frame.add(completenessLabel);
        bottomLabel.setBounds(1250, 5, 100, 20);
        completenessLabel.setBounds(1250, 35, 100, 20);
    }

    private void initEventListeners() {
        userInputEnterActionPerformed();
        loadFileActionPerformed();
        enterEquivalenceProofActionPerformed();
        saveProofActionPerformed();
        loadProofActionPerformed();
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

    private void saveProofActionPerformed() {
        saveProof.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                getPresenter().saveProof(file);
            }
        });
    }

    private void loadProofActionPerformed() {
        loadProof.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                getPresenter().loadProof(file);
            }
        });
    }

    private void upArrowKeyActionPerformed() {
        userInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    //getPresenter().getModel().addUserInput(userInput.getText());
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
    public void updateRulesLeftField(String left) {
        ruleTextAreaLeft.setText(left);
    }

    @Override
    public void updateRulesRightField(String right) {
        ruleTextAreaRight.setText(right);
    }

    @Override
    public void updateRulesConstraintField(String constraint) {
        ruleTextAreaConstraint.setText(constraint);
    }

    @Override
    public void updateEquationsLeftField(String left) {
        equationTextAreaLeft.setText(left);
    }

    @Override
    public void updateEquationsRightField(String right) {
        equationTextAreaRight.setText(right);
    }

    @Override
    public void updateEquationsConstraintField(String constraint) {
        equationTextAreaConstraint.setText(constraint);
    }

    @Override
    public void updateCompletenessField(boolean completeness) {
        _completeness.setSelected(completeness);
    }

    @Override
    public void updateBottomField(boolean bottom) {
        _bottom.setSelected(bottom);
    }

    @Override
    public void updatePositionsField(String positions) {
        positionTextArea.setText(positions);
    }
}

class Synchronizer implements AdjustmentListener {
    JScrollBar v1, h1, v2, h2, v3, h3;

    public Synchronizer(JScrollPane sp1, JScrollPane sp2, JScrollPane sp3) {
        v1 = sp1.getVerticalScrollBar();
        h1 = sp1.getHorizontalScrollBar();
        v2 = sp2.getVerticalScrollBar();
        h2 = sp2.getHorizontalScrollBar();
        v3 = sp3.getVerticalScrollBar();
        h3 = sp3.getHorizontalScrollBar();
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        JScrollBar bar = (JScrollBar) e.getSource();
        int val = bar.getValue();
        JScrollBar target1 = null;
        JScrollBar target2 = null;
        if (bar == v1) {
            target1 = v2;
            target2 = v3;
        }
        if (bar == h1) {
            target1 = h2;
            target2 = h3;
        }
        if (bar == v2) {
            target1 = v1;
            target2 = v3;
        }
        if (bar == h2) {
            target1 = h1;
            target2 = h3;
        }
        if (bar == v3) {
            target1 = v1;
            target2 = v2;
        }
        if (bar == h3) {
            target1 = h1;
            target2 = h2;
        }
        try {
            target1.setValue(val);
            target2.setValue(val);
        } catch (NullPointerException ex) {

        }
    }
}

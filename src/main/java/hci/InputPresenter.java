package hci;

import cora.exceptions.ParserException;
import hci.interfaces.UserInputModel;
import hci.interfaces.UserInputPresenter;

import java.io.File;

public class InputPresenter implements UserInputPresenter {
    private InputView view;
    private InputModel model;

    public InputPresenter(InputView view, InputModel model) {
        this.view = view;
        this.model = model;
        initController();
    }

    private void initController() {

    }

    @Override
    public UserInputModel getModel() {
        return model;
    }

    @Override
    public void setModel(InputModel inputModel) {
        model = inputModel;
    }

    @Override
    public void handleUserInput(String input) {
        if (model.addUserInput(input)) {
            view.onEnterAction();
            view.updateRulesField(model.getRules());
            view.updateEquationsField(model.getEquations());
        } else {
            view.onEnterAction();
            view.invalidRuleAction();
        }
    }

    @Override
    public void run() {
        view.setPresenter(this);
    }

    @Override
    public void handleFile(File file) {
        try {
            model.openFile(file);
            view.updateRulesField(model.getRules());
        } catch (RuntimeException e) {
            System.out.println(e.toString());
        }

    }

    @Override
    public void enterProof(String proof) {
        try {
            model.enterProof(proof);
            view.updateEquationsField(model.getEquations());
        } catch (ParserException e) {
            System.out.println(e.toString());
        }
    }
}

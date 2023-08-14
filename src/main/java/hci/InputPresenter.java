package hci;

import cora.exceptions.InvalidFileExtensionError;
import cora.exceptions.InvalidUserInputException;
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
            //view.updateRulesField(model.getRules());
            view.updateRulesLeftField(model.getRulesLeft());
            view.updateRulesRightField(model.getRulesRight());
            view.updateRulesConstraintField(model.getRulesConstraint());

            view.updateEquationsLeftField(model.getEquationsLeft());
            view.updateEquationsRightField(model.getEquationsRight());
            view.updateEquationsConstraintField(model.getEquationsConstraint());

            view.updateBottomField(model.getBottom());
            view.updateCompletenessField(model.getCompleteness());
        } else {
            view.onEnterAction();
        }
    }

    @Override
    public void run() {
        model.setPresenter(this);
        view.setPresenter(this);
    }

    @Override
    public void handleFile(File file) {
        try {
            model.openFile(file);
            //view.updateRulesField(model.getRules());
            view.updateRulesLeftField(model.getRulesLeft());
            view.updateRulesRightField(model.getRulesRight());
            view.updateRulesConstraintField(model.getRulesConstraint());
        } catch (RuntimeException | InvalidFileExtensionError e) {
            displayWarning(e.getMessage());
        }

    }

    @Override
    public void enterProof(String proof) {
        try {
            model.enterProof(proof);
            //view.updateEquationsField(model.getEquations());
            view.updateEquationsLeftField(model.getEquationsLeft());
            view.updateEquationsRightField(model.getEquationsRight());
            view.updateEquationsConstraintField(model.getEquationsConstraint());
        } catch (ParserException | InvalidUserInputException e) {
            displayWarning(e.getMessage());
        }
    }

    @Override
    public void displayWarning(String ex) {
        view.warningDialog(ex);
    }

    @Override
    public void saveProof(File file) {
        getModel().saveProofToFile(file);
    }

    @Override
    public void loadProof(File file) {
        getModel().loadProofFromFile(file);
    }
}

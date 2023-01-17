package hci;

import hci.interfaces.UserInputModel;
import hci.interfaces.UserInputPresenter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        return null;
    }

    @Override
    public void setModel(InputModel inputModel) {

    }

    @Override
    public void handleUserInput(String input) {
        if (model.addUserInput(input)) {
            view.onEnterAction();
        }
    }

    @Override
    public void run() {
        view.setPresenter(this);
    }

    @Override
    public void handleFile(File file) {
        model.openFile(file);
    }
}

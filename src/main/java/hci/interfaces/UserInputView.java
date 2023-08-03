package hci.interfaces;

import hci.InputPresenter;

public interface UserInputView {

    InputPresenter getPresenter();

    void setPresenter(InputPresenter inputPresenter);

    void onEnterAction();

    void updateRulesField(String rules);

    void updateEquationsField(String equations);

    void updateCompletenessField(boolean completeness);

    void updateBottomField(boolean bottom);

    void warningDialog(String ex);
}

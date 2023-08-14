package hci.interfaces;

import hci.InputPresenter;

public interface UserInputView {

    InputPresenter getPresenter();

    void setPresenter(InputPresenter inputPresenter);

    void onEnterAction();

    void updateRulesField(String rules);

    void updateRulesLeftField(String left);

    void updateRulesRightField(String right);

    void updateRulesConstraintField(String constraint);

    void updateEquationsField(String equations);

    void updateEquationsLeftField(String left);

    void updateEquationsRightField(String right);

    void updateEquationsConstraintField(String constraint);

    void updateCompletenessField(boolean completeness);

    void updateBottomField(boolean bottom);

    void warningDialog(String ex);
}

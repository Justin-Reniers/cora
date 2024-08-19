package hci.interfaces;

import hci.InputPresenter;

public interface UserInputView {

    InputPresenter getPresenter();

    void setPresenter(InputPresenter inputPresenter);

    void onEnterAction();

    void proofCompleteDialog(String msg);

    void updateRulesLeftField(String left);

    void updateRulesRightField(String right);

    void updateRulesConstraintField(String constraint);

    void updateEquationsLeftField(String left);

    void updateEquationsRightField(String right);

    void updateEquationsConstraintField(String constraint);

    void updateCompletenessField(boolean completeness);

    void updateBottomField(boolean bottom);

    void warningDialog(String ex);

    void updatePositionsField(String positions);
}

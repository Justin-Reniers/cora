package hci.interfaces;

import hci.InputPresenter;

public interface UserInputView {

    InputPresenter getPresenter();

    void setPresenter(InputPresenter inputPresenter);

    void onEnterAction();

    void invalidRuleAction();

    void updateRulesField(String rules);

    void updateEquationsField(String equations);
}

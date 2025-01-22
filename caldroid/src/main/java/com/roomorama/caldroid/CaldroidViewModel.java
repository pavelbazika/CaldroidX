package com.roomorama.caldroid;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CaldroidViewModel extends ViewModel {
    public static class CaldroidAction {
        public enum Action {
            DATE_SELECTED,
            MONTH_SELECTED,
            YEAR_SELECTED,
            DATE_TITLE_CLICKED,
            MONTH_TITLE_CLICKED
        }

        public CaldroidAction(Action _action, @NonNull Date _date) {
            mAction = _action;
            mDate = _date;
        }

        public Action getAction() {
            return mAction;
        }

        @NonNull
        public Date getDate() {
            return mDate;
        }

        private final Action mAction;
        private final Date mDate;
    }

    @NonNull
    public LiveData<CaldroidAction> getAction() {
        return mAction;
    }

    public void selectDate(@NonNull Date _date) {
        mAction.postValue(new CaldroidAction(CaldroidAction.Action.DATE_SELECTED, _date));
    }

    public void selectMonth(@NonNull Date _date) {
        mAction.postValue(new CaldroidAction(CaldroidAction.Action.MONTH_SELECTED, _date));
    }

    public void selectYear(@NonNull Date _date) {
        mAction.postValue(new CaldroidAction(CaldroidAction.Action.YEAR_SELECTED, _date));
    }

    public void dateTitleClicked(@NonNull Date _date) {
        mAction.postValue(new CaldroidAction(CaldroidAction.Action.DATE_TITLE_CLICKED, _date));
    }

    public void monthTitleClicked(@NonNull Date _date) {
        mAction.postValue(new CaldroidAction(CaldroidAction.Action.MONTH_TITLE_CLICKED, _date));
    }

    private final MutableLiveData<CaldroidAction> mAction = new MutableLiveData<>();
}

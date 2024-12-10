package org.smartregister.chw.hts.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.hts.contract.HtsRegisterContract;
import org.smartregister.chw.hts.util.AppExecutors;
import org.smartregister.chw.hts.util.HtsUtil;

public class BaseHtsRegisterInteractor implements HtsRegisterContract.Interactor {

    private AppExecutors appExecutors;

    @VisibleForTesting
    BaseHtsRegisterInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public BaseHtsRegisterInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void saveRegistration(final String jsonString, final HtsRegisterContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            try {
                HtsUtil.saveFormEvent(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }

            appExecutors.mainThread().execute(() -> callBack.onRegistrationSaved());
        };
        appExecutors.diskIO().execute(runnable);
    }
}

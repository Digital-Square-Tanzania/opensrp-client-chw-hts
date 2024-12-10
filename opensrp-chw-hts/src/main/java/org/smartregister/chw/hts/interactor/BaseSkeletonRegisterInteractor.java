package org.smartregister.chw.hts.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.hts.contract.SkeletonRegisterContract;
import org.smartregister.chw.hts.util.AppExecutors;
import org.smartregister.chw.hts.util.SkeletonUtil;

public class BaseSkeletonRegisterInteractor implements SkeletonRegisterContract.Interactor {

    private AppExecutors appExecutors;

    @VisibleForTesting
    BaseSkeletonRegisterInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public BaseSkeletonRegisterInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void saveRegistration(final String jsonString, final SkeletonRegisterContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            try {
                SkeletonUtil.saveFormEvent(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }

            appExecutors.mainThread().execute(() -> callBack.onRegistrationSaved());
        };
        appExecutors.diskIO().execute(runnable);
    }
}

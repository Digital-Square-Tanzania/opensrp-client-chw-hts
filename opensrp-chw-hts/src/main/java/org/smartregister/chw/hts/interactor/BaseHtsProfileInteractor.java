package org.smartregister.chw.hts.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.hts.contract.HtsProfileContract;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.util.AppExecutors;
import org.smartregister.chw.hts.util.HtsUtil;
import org.smartregister.domain.AlertStatus;

import java.util.Date;

public class BaseHtsProfileInteractor implements HtsProfileContract.Interactor {
    protected AppExecutors appExecutors;

    @VisibleForTesting
    BaseHtsProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public BaseHtsProfileInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void refreshProfileInfo(MemberObject memberObject, HtsProfileContract.InteractorCallBack callback) {
        Runnable runnable = () -> appExecutors.mainThread().execute(() -> {
            callback.refreshFamilyStatus(AlertStatus.normal);
            callback.refreshMedicalHistory(true);
            callback.refreshUpComingServicesStatus("Hts Visit", AlertStatus.normal, new Date());
        });
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveRegistration(final String jsonString, final HtsProfileContract.InteractorCallBack callback) {

        Runnable runnable = () -> {
            try {
                HtsUtil.saveFormEvent(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }

        };
        appExecutors.diskIO().execute(runnable);
    }
}

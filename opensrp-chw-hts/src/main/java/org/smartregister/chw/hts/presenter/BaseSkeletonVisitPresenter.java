package org.smartregister.chw.hts.presenter;

import org.json.JSONObject;
import org.smartregister.chw.hts.contract.BaseSkeletonVisitContract;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.model.BaseSkeletonVisitAction;
import org.smartregister.chw.hts.util.SkeletonJsonFormUtils;
import org.smartregister.util.FormUtils;
import org.smartregister.chw.hts.R;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;

import timber.log.Timber;

public class BaseSkeletonVisitPresenter implements BaseSkeletonVisitContract.Presenter, BaseSkeletonVisitContract.InteractorCallBack {

    protected WeakReference<BaseSkeletonVisitContract.View> view;
    protected BaseSkeletonVisitContract.Interactor interactor;
    protected MemberObject memberObject;

    public BaseSkeletonVisitPresenter(MemberObject memberObject, BaseSkeletonVisitContract.View view, BaseSkeletonVisitContract.Interactor interactor) {
        this.view = new WeakReference<>(view);
        this.interactor = interactor;
        this.memberObject = memberObject;
    }

    @Override
    public void startForm(String formName, String memberID, String currentLocationId) {
        try {
            if (view.get() != null) {
                JSONObject jsonObject = FormUtils.getInstance(view.get().getContext()).getFormJson(formName);
                SkeletonJsonFormUtils.getRegistrationForm(jsonObject, memberID, currentLocationId);
                view.get().startFormActivity(jsonObject);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public boolean validateStatus() {
        return false;
    }

    @Override
    public void initialize() {
        view.get().displayProgressBar(true);
        view.get().redrawHeader(memberObject);
        interactor.calculateActions(view.get(), memberObject, this);
    }

    @Override
    public void submitVisit() {
        if (view.get() != null) {
            view.get().displayProgressBar(true);
            interactor.submitVisit(view.get().getEditMode(), memberObject.getBaseEntityId(), view.get().getSkeletonVisitActions(), this);
        }
    }

    @Override
    public void reloadMemberDetails(String memberID, String profileType) {
        view.get().displayProgressBar(true);
        interactor.reloadMemberDetails(memberID, profileType, this);
    }

    @Override
    public void onMemberDetailsReloaded(MemberObject memberObject) {
        if (view.get() != null) {
            this.memberObject = memberObject;

            view.get().displayProgressBar(false);
            view.get().onMemberDetailsReloaded(memberObject);
        }
    }

    @Override
    public void onRegistrationSaved(boolean isEdit) {
        Timber.v("onRegistrationSaved");
    }

    @Override
    public void preloadActions(LinkedHashMap<String, BaseSkeletonVisitAction> map) {
        if (view.get() != null)
            view.get().initializeActions(map);
    }

    @Override
    public void onSubmitted(String results) {
        if (view.get() != null) {
            view.get().displayProgressBar(false);
            if (results != null) {
                view.get().submittedAndClose(results);
            } else {
                view.get().displayToast(view.get().getContext().getString(R.string.error_unable_save_visit));
            }
        }
    }
}

package org.smartregister.chw.hts.contract;

import android.content.Context;

import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.model.BaseHtsVisitAction;

import java.util.LinkedHashMap;
import java.util.Map;

public interface BaseHtsVisitContract {
    interface View extends VisitView {

        Presenter presenter();

        Form getFormConfig();

        void startForm(BaseHtsVisitAction htsHomeVisitAction);

        void startFormActivity(JSONObject jsonForm);

        void startFragment(BaseHtsVisitAction htsHomeVisitAction);

        void redrawHeader(MemberObject memberObject);

        void redrawVisitUI();

        void displayProgressBar(boolean state);

        Map<String, BaseHtsVisitAction> getHtsVisitActions();

        void close();

        void submittedAndClose(String results);

        Presenter getPresenter();

        /**
         * Save the received data into the events table
         * Start aggregation of all events and persist results into the events table
         */
        void submitVisit();

        void initializeActions(LinkedHashMap<String, BaseHtsVisitAction> map);

        Context getContext();

        void displayToast(String message);

        Boolean getEditMode();

        void onMemberDetailsReloaded(MemberObject memberObject);
    }

    interface VisitView {

        /**
         * Results action when a dialog is opened and returns a payload
         *
         * @param jsonString
         */
        void onDialogOptionUpdated(String jsonString);

        Context getMyContext();
    }

    interface Presenter {

        void startForm(String formName, String memberID, String currentLocationId);

        /**
         * Recall this method to redraw ui after every submission
         *
         * @return
         */
        boolean validateStatus();

        /**
         * Preload header and visit
         */
        void initialize();

        void submitVisit();

        void reloadMemberDetails(String memberID, String profileType);
    }

    interface Model {

        JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception;

    }

    interface Interactor {

        void reloadMemberDetails(String memberID, String profileType, InteractorCallBack callBack);

        MemberObject getMemberClient(String memberID, String profileType);

        void saveRegistration(String jsonString, boolean isEditMode, final InteractorCallBack callBack);

        void calculateActions(View view, MemberObject memberObject, InteractorCallBack callBack);

        void submitVisit(boolean editMode, String memberID, Map<String, BaseHtsVisitAction> map, InteractorCallBack callBack);
    }

    interface InteractorCallBack {

        void onMemberDetailsReloaded(MemberObject memberObject);

        void onRegistrationSaved(boolean isEdit);

        void preloadActions(LinkedHashMap<String, BaseHtsVisitAction> map);

        void onSubmitted(String results);
    }
}

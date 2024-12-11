package org.smartregister.chw.hts.interactor;


import android.content.Context;

import androidx.annotation.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hts.R;
import org.smartregister.chw.hts.HtsLibrary;
import org.smartregister.chw.hts.actionhelper.VisitTypeActionHelper;
import org.smartregister.chw.hts.actionhelper.HtsMedicalHistoryActionHelper;
import org.smartregister.chw.hts.contract.BaseHtsVisitContract;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.domain.VisitDetail;
import org.smartregister.chw.hts.model.BaseHtsVisitAction;
import org.smartregister.chw.hts.util.AppExecutors;
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class BaseHtsServiceVisitInteractor extends BaseHtsVisitInteractor {

    protected BaseHtsVisitContract.InteractorCallBack callBack;

    String visitType;
    private final HtsLibrary htsLibrary;
    private final LinkedHashMap<String, BaseHtsVisitAction> actionList;
    protected AppExecutors appExecutors;
    private ECSyncHelper syncHelper;
    private Context mContext;


    @VisibleForTesting
    public BaseHtsServiceVisitInteractor(AppExecutors appExecutors, HtsLibrary HtsLibrary, ECSyncHelper syncHelper) {
        this.appExecutors = appExecutors;
        this.htsLibrary = HtsLibrary;
        this.syncHelper = syncHelper;
        this.actionList = new LinkedHashMap<>();
    }

    public BaseHtsServiceVisitInteractor(String visitType) {
        this(new AppExecutors(), HtsLibrary.getInstance(), HtsLibrary.getInstance().getEcSyncHelper());
        this.visitType = visitType;
    }

    @Override
    protected String getCurrentVisitType() {
        if (StringUtils.isNotBlank(visitType)) {
            return visitType;
        }
        return super.getCurrentVisitType();
    }

    @Override
    protected void populateActionList(BaseHtsVisitContract.InteractorCallBack callBack) {
        this.callBack = callBack;
        final Runnable runnable = () -> {
            try {
                evaluateHtsMedicalHistory(details);
                evaluateHtsPhysicalExam(details);
                evaluateVisitType(details);

            } catch (BaseHtsVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateHtsMedicalHistory(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {

        HtsMedicalHistoryActionHelper actionHelper = new HtsMedicalHistory(mContext, memberObject);
        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_medical_history))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.Hts_FOLLOWUP_FORMS.MEDICAL_HISTORY)
                .build();
        actionList.put(context.getString(R.string.hts_medical_history), action);

    }

    private void evaluateHtsPhysicalExam(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {

        HtsPhysicalExamActionHelper actionHelper = new HtsPhysicalExamActionHelper(mContext, memberObject);
        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_physical_examination))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.Hts_FOLLOWUP_FORMS.PHYSICAL_EXAMINATION)
                .build();
        actionList.put(context.getString(R.string.hts_physical_examination), action);
    }

    private void evaluateVisitType(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        VisitTypeActionHelper actionHelper = new VisitTypeActionHelper(mContext, memberObject);
        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_visit_type_action_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_VISIT_TYPE)
                .build();
        actionList.put(context.getString(R.string.hts_visit_type_action_title), action);
    }

    @Override
    protected String getEncounterType() {
        return Constants.EVENT_TYPE.HTS_SERVICES;
    }

    @Override
    protected String getTableName() {
        return Constants.TABLES.Hts_SERVICE;
    }

    private class HtsMedicalHistory extends HtsMedicalHistoryActionHelper {


        public HtsMedicalHistory(Context context, MemberObject memberObject) {
            super(context, memberObject);
        }

        @Override
        public String postProcess(String s) {
            if (StringUtils.isNotBlank(medical_history)) {
                try {
                    evaluateHtsPhysicalExam(details);
                    evaluateVisitType(details);
                } catch (BaseHtsVisitAction.ValidationException e) {
                    e.printStackTrace();
                }
            }
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }

    }

    private class HtsPhysicalExamActionHelper extends org.smartregister.chw.hts.actionhelper.HtsPhysicalExamActionHelper {

        public HtsPhysicalExamActionHelper(Context context, MemberObject memberObject) {
            super(context, memberObject);
        }

        @Override
        public String postProcess(String s) {
            if (StringUtils.isNotBlank(medical_history)) {
                try {
                    evaluateVisitType(details);
                } catch (BaseHtsVisitAction.ValidationException e) {
                    e.printStackTrace();
                }
            }
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }

    }

}

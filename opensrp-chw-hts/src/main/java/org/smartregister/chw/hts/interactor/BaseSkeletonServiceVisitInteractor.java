package org.smartregister.chw.hts.interactor;


import android.content.Context;

import androidx.annotation.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hts.R;
import org.smartregister.chw.hts.HtsLibrary;
import org.smartregister.chw.hts.actionhelper.SkeletonHtsActionHelper;
import org.smartregister.chw.hts.actionhelper.SkeletonMedicalHistoryActionHelper;
import org.smartregister.chw.hts.contract.BaseSkeletonVisitContract;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.domain.VisitDetail;
import org.smartregister.chw.hts.model.BaseSkeletonVisitAction;
import org.smartregister.chw.hts.util.AppExecutors;
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class BaseSkeletonServiceVisitInteractor extends BaseSkeletonVisitInteractor {

    protected BaseSkeletonVisitContract.InteractorCallBack callBack;

    String visitType;
    private final HtsLibrary htsLibrary;
    private final LinkedHashMap<String, BaseSkeletonVisitAction> actionList;
    protected AppExecutors appExecutors;
    private ECSyncHelper syncHelper;
    private Context mContext;


    @VisibleForTesting
    public BaseSkeletonServiceVisitInteractor(AppExecutors appExecutors, HtsLibrary HtsLibrary, ECSyncHelper syncHelper) {
        this.appExecutors = appExecutors;
        this.htsLibrary = HtsLibrary;
        this.syncHelper = syncHelper;
        this.actionList = new LinkedHashMap<>();
    }

    public BaseSkeletonServiceVisitInteractor(String visitType) {
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
    protected void populateActionList(BaseSkeletonVisitContract.InteractorCallBack callBack) {
        this.callBack = callBack;
        final Runnable runnable = () -> {
            try {
                evaluateSkeletonMedicalHistory(details);
                evaluateSkeletonPhysicalExam(details);
                evaluateSkeletonHTS(details);

            } catch (BaseSkeletonVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateSkeletonMedicalHistory(Map<String, List<VisitDetail>> details) throws BaseSkeletonVisitAction.ValidationException {

        SkeletonMedicalHistoryActionHelper actionHelper = new SkeletonMedicalHistory(mContext, memberObject);
        BaseSkeletonVisitAction action = getBuilder(context.getString(R.string.hts_medical_history))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.SKELETON_FOLLOWUP_FORMS.MEDICAL_HISTORY)
                .build();
        actionList.put(context.getString(R.string.hts_medical_history), action);

    }

    private void evaluateSkeletonPhysicalExam(Map<String, List<VisitDetail>> details) throws BaseSkeletonVisitAction.ValidationException {

        SkeletonPhysicalExamActionHelper actionHelper = new SkeletonPhysicalExamActionHelper(mContext, memberObject);
        BaseSkeletonVisitAction action = getBuilder(context.getString(R.string.hts_physical_examination))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.SKELETON_FOLLOWUP_FORMS.PHYSICAL_EXAMINATION)
                .build();
        actionList.put(context.getString(R.string.hts_physical_examination), action);
    }

    private void evaluateSkeletonHTS(Map<String, List<VisitDetail>> details) throws BaseSkeletonVisitAction.ValidationException {

        SkeletonHtsActionHelper actionHelper = new SkeletonHtsActionHelper(mContext, memberObject);
        BaseSkeletonVisitAction action = getBuilder(context.getString(R.string.hts_hts))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.SKELETON_FOLLOWUP_FORMS.HTS)
                .build();
        actionList.put(context.getString(R.string.hts_hts), action);
    }

    @Override
    protected String getEncounterType() {
        return Constants.EVENT_TYPE.SKELETON_SERVICES;
    }

    @Override
    protected String getTableName() {
        return Constants.TABLES.SKELETON_SERVICE;
    }

    private class SkeletonMedicalHistory extends org.smartregister.chw.hts.actionhelper.SkeletonMedicalHistoryActionHelper {


        public SkeletonMedicalHistory(Context context, MemberObject memberObject) {
            super(context, memberObject);
        }

        @Override
        public String postProcess(String s) {
            if (StringUtils.isNotBlank(medical_history)) {
                try {
                    evaluateSkeletonPhysicalExam(details);
                    evaluateSkeletonHTS(details);
                } catch (BaseSkeletonVisitAction.ValidationException e) {
                    e.printStackTrace();
                }
            }
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }

    }

    private class SkeletonPhysicalExamActionHelper extends org.smartregister.chw.hts.actionhelper.SkeletonPhysicalExamActionHelper {

        public SkeletonPhysicalExamActionHelper(Context context, MemberObject memberObject) {
            super(context, memberObject);
        }

        @Override
        public String postProcess(String s) {
            if (StringUtils.isNotBlank(medical_history)) {
                try {
                    evaluateSkeletonHTS(details);
                } catch (BaseSkeletonVisitAction.ValidationException e) {
                    e.printStackTrace();
                }
            }
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));
            return super.postProcess(s);
        }

    }

}

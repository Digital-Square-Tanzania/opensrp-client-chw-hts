package org.smartregister.chw.hts.interactor;


import android.content.Context;

import androidx.annotation.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hts.HtsLibrary;
import org.smartregister.chw.hts.R;
import org.smartregister.chw.hts.actionhelper.DnaPcrSampleCollectionActionHelper;
import org.smartregister.chw.hts.actionhelper.HivTestingActionHelper;
import org.smartregister.chw.hts.actionhelper.LinkageToPreventionServicesActionHelper;
import org.smartregister.chw.hts.actionhelper.PostTestServicesActionHelper;
import org.smartregister.chw.hts.actionhelper.PreTestServicesActionHelper;
import org.smartregister.chw.hts.actionhelper.VisitTypeActionHelper;
import org.smartregister.chw.hts.contract.BaseHtsVisitContract;
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

    private final LinkedHashMap<String, BaseHtsVisitAction> actionList;
    protected BaseHtsVisitContract.InteractorCallBack callBack;
    protected AppExecutors appExecutors;
    String visitType;
    private Context mContext;


    @VisibleForTesting
    public BaseHtsServiceVisitInteractor(AppExecutors appExecutors, HtsLibrary HtsLibrary, ECSyncHelper syncHelper) {
        this.appExecutors = appExecutors;
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
                evaluateVisitType(details);
            } catch (BaseHtsVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateVisitType(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        VisitTypeActionHelper actionHelper = new VisitTypeActionHelper(mContext, memberObject) {
            @Override
            public void processVisitType(String visitType) {
                try {
                    evaluatePreTestServices(details);
                    evaluateHivTestingServices(details);
                    evaluatePostTestServices(details);
                    evaluateDnaPcrSampleCollection(details);
                    evaluateLinkageToPreventionServices(details);
                } catch (Exception e) {
                    Timber.e(e);
                }
                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };

        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_visit_type_action_title))
                .withOptional(false)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_VISIT_TYPE)
                .build();
        actionList.put(context.getString(R.string.hts_visit_type_action_title), action);
    }

    private void evaluatePreTestServices(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        PreTestServicesActionHelper actionHelper = new PreTestServicesActionHelper(mContext, memberObject);
        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_pre_test_services_action_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_PRE_TEST_SERVICES)
                .build();
        actionList.put(context.getString(R.string.hts_pre_test_services_action_title), action);
    }

    private void evaluateHivTestingServices(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        HivTestingActionHelper actionHelper = new HivTestingActionHelper(mContext, memberObject);
        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_hiv_testing_action_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_HIV_TESTING_SERVICES)
                .build();
        actionList.put(context.getString(R.string.hts_hiv_testing_action_title), action);
    }

    private void evaluatePostTestServices(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        PostTestServicesActionHelper actionHelper = new PostTestServicesActionHelper(mContext, memberObject);
        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_post_test_services_action_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_POST_TEST_SERVICES)
                .build();
        actionList.put(context.getString(R.string.hts_post_test_services_action_title), action);
    }

    private void evaluateDnaPcrSampleCollection(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        DnaPcrSampleCollectionActionHelper actionHelper = new DnaPcrSampleCollectionActionHelper(mContext, memberObject);
        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_dna_pcr_sample_collection_action_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_DNA_PCR_SAMPLE_COLLECTION)
                .build();
        actionList.put(context.getString(R.string.hts_dna_pcr_sample_collection_action_title), action);
    }

    private void evaluateLinkageToPreventionServices(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        LinkageToPreventionServicesActionHelper actionHelper = new LinkageToPreventionServicesActionHelper(mContext, memberObject);
        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_linkage_to_prevention_services_action_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_LINKAGE_TO_PREVENTION_SERVICES)
                .build();
        actionList.put(context.getString(R.string.hts_linkage_to_prevention_services_action_title), action);
    }

    @Override
    protected String getEncounterType() {
        return Constants.EVENT_TYPE.HTS_SERVICES;
    }

    @Override
    protected String getTableName() {
        return Constants.TABLES.HTS_SERVICES;
    }

}

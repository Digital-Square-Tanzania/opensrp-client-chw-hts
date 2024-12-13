package org.smartregister.chw.hts.interactor;


import android.content.Context;

import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hts.HtsLibrary;
import org.smartregister.chw.hts.R;
import org.smartregister.chw.hts.actionhelper.DnaPcrSampleCollectionActionHelper;
import org.smartregister.chw.hts.actionhelper.HivFirstHivTestActionHelper;
import org.smartregister.chw.hts.actionhelper.HivRepeatFirstHivTestActionHelper;
import org.smartregister.chw.hts.actionhelper.HivSecondHivTestActionHelper;
import org.smartregister.chw.hts.actionhelper.HivUnigoldHivTestActionHelper;
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
                    evaluateFirstHivTest(details, 1);
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

    private void evaluateFirstHivTest(Map<String, List<VisitDetail>> details, final int repeatNumber) throws BaseHtsVisitAction.ValidationException {
        HivFirstHivTestActionHelper actionHelper = new HivFirstHivTestActionHelper(mContext, memberObject) {
            @Override
            public void processFirstHivTestResults(String firstHivTestResults) {
                try {
                    if (firstHivTestResults.equalsIgnoreCase("reactive")) {
                        evaluateSecondHivTest(details, 1);

                        removeCommonActions();
                        removeExtraRepeatActions(R.string.hts_repeate_of_first_hiv_test_action_title, repeatNumber);
                    } else if (firstHivTestResults.equalsIgnoreCase("non_reactive")) {
                        evaluatePostTestServices(details);
                        evaluateLinkageToPreventionServices(details);

                        actionList.remove(context.getString(R.string.hts_second_hiv_test_action_title));
                        removeExtraRepeatActions(R.string.hts_repeate_of_first_hiv_test_action_title, repeatNumber);
                    } else {
                        evaluateFirstHivTest(details, repeatNumber + 1);

                        removeCommonActions();
                        actionList.remove(context.getString(R.string.hts_second_hiv_test_action_title));
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };

        String actionTitle = context.getString(R.string.hts_first_hiv_test_action_title);
        if (repeatNumber != 1) {
            actionTitle = String.format(context.getString(R.string.hts_repeate_of_first_hiv_test_action_title), repeatNumber);
        }
        BaseHtsVisitAction action = getBuilder(actionTitle)
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_FIRST_HIV_TEST)
                .build();
        actionList.put(actionTitle, action);
    }

    private void evaluateSecondHivTest(Map<String, List<VisitDetail>> details, int repeatNumber) throws BaseHtsVisitAction.ValidationException {
        HivSecondHivTestActionHelper actionHelper = new HivSecondHivTestActionHelper(mContext, memberObject) {
            @Override
            public void processSecondHivTestResults(String secondHivTestResults) {
                try {
                    if (secondHivTestResults.equalsIgnoreCase("reactive")) {
                        evaluateUnigoldHivTest(details);
                    } else if (secondHivTestResults.equalsIgnoreCase("non_reactive")) {
                        evaluateRepeatOfFirstHivTest(details);
                    } else {
                        evaluateSecondHivTest(details, repeatNumber + 1);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };

        String actionTitle = context.getString(R.string.hts_second_hiv_test_action_title);
        if (repeatNumber != 1) {
            actionTitle = String.format(context.getString(R.string.hts_repeate_of_second_hiv_test_action_title), repeatNumber);
        }

        BaseHtsVisitAction action = getBuilder(actionTitle)
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_SECOND_HIV_TEST)
                .build();
        actionList.put(actionTitle, action);
    }

    private void evaluateUnigoldHivTest(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        HivUnigoldHivTestActionHelper actionHelper = new HivUnigoldHivTestActionHelper(mContext, memberObject) {
            @Override
            public void processUnigoldHivTestResults(String unigoldHivTestResults) {
                try {
                    if (unigoldHivTestResults.equalsIgnoreCase("reactive")) {
                        evaluatePostTestServices(details);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };

        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_unigold_hiv_test_action_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_UNIGOLD_HIV_TEST)
                .build();
        actionList.put(context.getString(R.string.hts_unigold_hiv_test_action_title), action);
    }


    private void evaluateRepeatOfFirstHivTest(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        HivRepeatFirstHivTestActionHelper actionHelper = new HivRepeatFirstHivTestActionHelper(mContext, memberObject) {
            @Override
            public void processFirstHivTestResults(String firstHivTestResults) {
                try {
                    if (firstHivTestResults.equalsIgnoreCase("non_reactive")) {
                        evaluatePostTestServices(details);
                        evaluateLinkageToPreventionServices(details);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };

        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_first_hiv_test_action_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_FIRST_HIV_TEST)
                .build();
        actionList.put(context.getString(R.string.hts_first_hiv_test_action_title), action);
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

    private void removeExtraRepeatActions(@StringRes int resId, int currentRepeatNumber) {
        for (int testNumber = currentRepeatNumber; testNumber <= actionList.size(); testNumber++) {
            actionList.remove(String.format(context.getString(resId), testNumber));
        }
    }

    private void removeCommonActions() {
        actionList.remove(context.getString(R.string.hts_post_test_services_action_title));
        actionList.remove(context.getString(R.string.hts_linkage_to_prevention_services_action_title));
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

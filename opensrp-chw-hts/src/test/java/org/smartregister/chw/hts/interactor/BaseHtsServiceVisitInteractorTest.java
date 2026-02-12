package org.smartregister.chw.hts.interactor;

import android.content.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.smartregister.chw.hts.R;
import org.smartregister.chw.hts.actionhelper.HivRepeatFirstHivTestActionHelper;
import org.smartregister.chw.hts.contract.BaseHtsVisitContract;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.domain.VisitDetail;
import org.smartregister.chw.hts.model.BaseHtsVisitAction;
import org.smartregister.chw.hts.util.AppExecutors;
import org.smartregister.chw.hts.util.Constants;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.mockito.Mockito.when;

public class BaseHtsServiceVisitInteractorTest {

    private BaseHtsServiceVisitInteractor interactor;
    private LinkedHashMap<String, BaseHtsVisitAction> actionList;
    private String repeatFirstActionTitle;
    private String repeatFirstActionTitleEntityTypeFormat;
    private String postTestActionTitle;
    private String linkageActionTitle;
    private String dnaActionTitle;
    private String preTestActionTitle;

    @Before
    public void setUp() {
        Context context = Mockito.mock(Context.class);
        when(context.getString(R.string.hts_repeate_of_first_hiv_test_title)).thenReturn("Repeat of First HIV Test");
        when(context.getString(R.string.hts_repeate_of_first_hiv_test_action_entity_type)).thenReturn("Repeat of First HIV Test %d");
        when(context.getString(R.string.hts_post_test_services_action_title)).thenReturn("Post Test Services");
        when(context.getString(R.string.hts_linkage_to_prevention_services_action_title)).thenReturn("Linkage To Prevention Services");
        when(context.getString(R.string.hts_dna_pcr_sample_collection_action_title)).thenReturn("DNA PCR Sample Collection");
        when(context.getString(R.string.hts_pre_test_services_action_title)).thenReturn("Pre-Test Services");

        AppExecutors instantExecutors = new AppExecutors(Runnable::run, Runnable::run, Runnable::run);
        interactor = Whitebox.newInstance(BaseHtsServiceVisitInteractor.class);

        Whitebox.setInternalState(interactor, "actionList", new LinkedHashMap<String, BaseHtsVisitAction>());
        Whitebox.setInternalState(interactor, "context", context);
        Whitebox.setInternalState(interactor, "mContext", context);
        Whitebox.setInternalState(interactor, "memberObject", new MemberObject());
        Whitebox.setInternalState(interactor, "callBack", Mockito.mock(BaseHtsVisitContract.InteractorCallBack.class));
        Whitebox.setInternalState(interactor, "appExecutors", instantExecutors);
        Whitebox.setInternalState(interactor, "mVisitType", "");
        Whitebox.setInternalState(interactor, "mClientType", "");

        actionList = Whitebox.getInternalState(interactor, "actionList");
        actionList.clear();

        repeatFirstActionTitle = context.getString(R.string.hts_repeate_of_first_hiv_test_title);
        repeatFirstActionTitleEntityTypeFormat = context.getString(R.string.hts_repeate_of_first_hiv_test_action_entity_type);
        postTestActionTitle = context.getString(R.string.hts_post_test_services_action_title);
        linkageActionTitle = context.getString(R.string.hts_linkage_to_prevention_services_action_title);
        dnaActionTitle = context.getString(R.string.hts_dna_pcr_sample_collection_action_title);
        preTestActionTitle = context.getString(R.string.hts_pre_test_services_action_title);
    }

    @Test
    public void invalidResultRepeatsRepeatFirstHivTestAction() throws Exception {
        evaluateRepeatFirstHivTestAction();
        actionList.put(postTestActionTitle, Mockito.mock(BaseHtsVisitAction.class));
        actionList.put(linkageActionTitle, Mockito.mock(BaseHtsVisitAction.class));
        actionList.put(dnaActionTitle, Mockito.mock(BaseHtsVisitAction.class));

        BaseHtsVisitAction initialAction = actionList.get(repeatFirstActionTitle);
        ((HivRepeatFirstHivTestActionHelper) initialAction.getHtsVisitActionHelper())
                .processFirstHivTestResults(Constants.HIV_TEST_RESULTS.INVALID);

        BaseHtsVisitAction repeatedAction = actionList.get(repeatFirstActionTitle);
        String secondRepeatTitle = String.format(repeatFirstActionTitleEntityTypeFormat, 2);
        BaseHtsVisitAction secondRepeatAction = actionList.get(secondRepeatTitle);
        Assert.assertSame(initialAction, repeatedAction);
        Assert.assertNotNull(secondRepeatAction);
        Assert.assertNotSame(initialAction, secondRepeatAction);
        Assert.assertFalse(actionList.containsKey(postTestActionTitle));
        Assert.assertFalse(actionList.containsKey(linkageActionTitle));
        Assert.assertFalse(actionList.containsKey(dnaActionTitle));
    }

    @Test
    public void wastageResultRepeatsRepeatFirstHivTestAction() throws Exception {
        evaluateRepeatFirstHivTestAction();
        actionList.put(postTestActionTitle, Mockito.mock(BaseHtsVisitAction.class));
        actionList.put(linkageActionTitle, Mockito.mock(BaseHtsVisitAction.class));
        actionList.put(dnaActionTitle, Mockito.mock(BaseHtsVisitAction.class));

        BaseHtsVisitAction initialAction = actionList.get(repeatFirstActionTitle);
        ((HivRepeatFirstHivTestActionHelper) initialAction.getHtsVisitActionHelper())
                .processFirstHivTestResults(Constants.HIV_TEST_RESULTS.WASTAGE);

        BaseHtsVisitAction repeatedAction = actionList.get(repeatFirstActionTitle);
        String secondRepeatTitle = String.format(repeatFirstActionTitleEntityTypeFormat, 2);
        BaseHtsVisitAction secondRepeatAction = actionList.get(secondRepeatTitle);
        Assert.assertSame(initialAction, repeatedAction);
        Assert.assertNotNull(secondRepeatAction);
        Assert.assertNotSame(initialAction, secondRepeatAction);
        Assert.assertFalse(actionList.containsKey(postTestActionTitle));
        Assert.assertFalse(actionList.containsKey(linkageActionTitle));
        Assert.assertFalse(actionList.containsKey(dnaActionTitle));
    }

    @Test
    public void nonReactiveResultKeepsNormalFlowWithoutRepeatingAction() throws Exception {
        evaluateRepeatFirstHivTestAction();
        actionList.put(dnaActionTitle, Mockito.mock(BaseHtsVisitAction.class));

        BaseHtsVisitAction initialAction = actionList.get(repeatFirstActionTitle);
        ((HivRepeatFirstHivTestActionHelper) initialAction.getHtsVisitActionHelper())
                .processFirstHivTestResults(Constants.HIV_TEST_RESULTS.NON_REACTIVE);

        BaseHtsVisitAction currentAction = actionList.get(repeatFirstActionTitle);
        Assert.assertSame(initialAction, currentAction);
        Assert.assertFalse(actionList.containsKey(String.format(repeatFirstActionTitleEntityTypeFormat, 2)));
        Assert.assertTrue(actionList.containsKey(postTestActionTitle));
        Assert.assertTrue(actionList.containsKey(linkageActionTitle));
        Assert.assertFalse(actionList.containsKey(dnaActionTitle));
    }

    @Test
    public void shouldReadLatestTbOutcomeFromPreTestActionPayload() throws Exception {
        BaseHtsVisitAction preTestAction = Mockito.mock(BaseHtsVisitAction.class);
        when(preTestAction.getJsonPayload()).thenReturn(createPreTestPayload("tb_suspect"));
        actionList.put(preTestActionTitle, preTestAction);

        String outcome = Whitebox.invokeMethod(interactor, "getLatestPreTestTbScreeningOutcomeFromActionPayload");

        Assert.assertEquals("tb_suspect", outcome);
    }

    @Test
    public void shouldReturnNullWhenPreTestOutcomeMissingFromPayload() throws Exception {
        BaseHtsVisitAction preTestAction = Mockito.mock(BaseHtsVisitAction.class);
        when(preTestAction.getJsonPayload()).thenReturn(createPreTestPayload(null));
        actionList.put(preTestActionTitle, preTestAction);

        String outcome = Whitebox.invokeMethod(interactor, "getLatestPreTestTbScreeningOutcomeFromActionPayload");

        Assert.assertNull(outcome);
    }

    private void evaluateRepeatFirstHivTestAction() throws Exception {
        Whitebox.invokeMethod(interactor, "evaluateRepeatOfFirstHivTest", new HashMap<String, List<VisitDetail>>());
    }

    private String createPreTestPayload(String tbOutcome) throws Exception {
        JSONObject field = new JSONObject();
        field.put("key", "hts_clients_tb_screening_outcome");
        if (tbOutcome != null) {
            field.put("value", tbOutcome);
        }

        JSONObject stepOne = new JSONObject();
        stepOne.put("fields", new JSONArray().put(field));

        return new JSONObject()
                .put("count", "1")
                .put("step1", stepOne)
                .toString();
    }
}

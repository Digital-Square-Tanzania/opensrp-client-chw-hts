package org.smartregister.chw.hts.dao;

import org.junit.Assert;
import org.junit.Test;
import org.smartregister.chw.hts.util.Constants;

public class HtsDaoCloseClientsQueryTest {

    @Test
    public void shouldApplyStrictMoreThanSevenDaysThresholdInQuery() {
        long nowMillis = 1700000000000L;

        String query = HtsDao.buildCloseHtsClientsWithFinalTestResultsSubQuery(nowMillis);

        Assert.assertTrue(query.contains("(" + nowMillis + " - CAST(trim(latest.last_interacted_with) AS INTEGER)) > "
                + HtsDao.FINAL_TEST_RESULT_CLOSURE_THRESHOLD_MILLIS));
        Assert.assertFalse(query.contains(">= " + HtsDao.FINAL_TEST_RESULT_CLOSURE_THRESHOLD_MILLIS));
    }

    @Test
    public void shouldGuardAgainstNullOrInvalidLastInteractedWithValuesInQuery() {
        String query = HtsDao.buildCloseHtsClientsWithFinalTestResultsSubQuery(1700000000000L);

        Assert.assertTrue(query.contains("trim(ifnull(latest.last_interacted_with, '')) != ''"));
        Assert.assertTrue(query.contains("trim(ifnull(latest.last_interacted_with, '')) NOT GLOB '*[^0-9]*'"));
    }

    @Test
    public void shouldKeepFinalResultMatchingConditionsUnchanged() {
        String query = HtsDao.buildCloseHtsClientsWithFinalTestResultsSubQuery(1700000000000L);

        Assert.assertTrue(query.contains("lower(ifnull(latest.test_result, '')) = '" + Constants.HIV_TEST_RESULTS.REACTIVE + "'"));
        Assert.assertTrue(query.contains("lower(ifnull(latest.test_type, '')) = 'unigold hiv test'"));

        Assert.assertTrue(query.contains("lower(ifnull(latest.test_result, '')) = '" + Constants.HIV_TEST_RESULTS.NON_REACTIVE + "'"));
        Assert.assertTrue(query.contains("lower(ifnull(latest.test_type, '')) = 'first hiv test'"));
        Assert.assertTrue(query.contains("lower(ifnull(latest.test_type, '')) = 'repeat first hiv test'"));
    }
}

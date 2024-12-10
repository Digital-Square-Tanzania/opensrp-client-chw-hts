package org.smartregister.chw.hts.model;

import org.json.JSONObject;
import org.smartregister.chw.hts.contract.HtsRegisterContract;
import org.smartregister.chw.hts.util.HtsJsonFormUtils;

public class BaseHtsRegisterModel implements HtsRegisterContract.Model {

    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject jsonObject = HtsJsonFormUtils.getFormAsJson(formName);
        HtsJsonFormUtils.getRegistrationForm(jsonObject, entityId, currentLocationId);

        return jsonObject;
    }

}

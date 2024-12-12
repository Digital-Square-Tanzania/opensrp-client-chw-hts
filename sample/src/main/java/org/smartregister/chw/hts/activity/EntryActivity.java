package org.smartregister.chw.hts.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import org.smartregister.chw.hts.contract.BaseHtsVisitContract;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.util.DBConstants;
import org.smartregister.chw.hts.R;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.activity.SecuredActivity;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class EntryActivity extends SecuredActivity implements View.OnClickListener, BaseHtsVisitContract.VisitView {
    private static MemberObject htsMemberObject;

    public static MemberObject getSampleMember() {
        if (htsMemberObject == null) {
            htsMemberObject = new MemberObject();
            htsMemberObject.setFirstName("Glory");
            htsMemberObject.setLastName("Juma");
            htsMemberObject.setMiddleName("Ali");
            htsMemberObject.setGender("Female");
            htsMemberObject.setMartialStatus("Married");
            htsMemberObject.setDob("1982-01-18T03:00:00.000+03:00");
            htsMemberObject.setAddress("Kijiweni");
            htsMemberObject.setUniqueId("3503504");
            htsMemberObject.setBaseEntityId("3503504");
            htsMemberObject.setFamilyBaseEntityId("3503504");
        }

        return htsMemberObject;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.hts_activity).setOnClickListener(this);
        findViewById(R.id.hts_home_visit).setOnClickListener(this);
        findViewById(R.id.hts_profile).setOnClickListener(this);
    }

    @Override
    protected void onCreation() {
        Timber.v("onCreation");
    }

    @Override
    protected void onResumption() {
        Timber.v("onCreation");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hts_activity:
                startActivity(new Intent(this, HtsRegisterActivity.class));
                break;
            case R.id.hts_home_visit:
                HtsServiceActivity.startHtsVisitActivity(this, "12345", true);
                break;
            case R.id.hts_profile:
                HtsMemberProfileActivity.startMe(this, "12345");
                break;
            default:
                break;
        }
    }

    @Override
    public void onDialogOptionUpdated(String jsonString) {
        Timber.v("onDialogOptionUpdated %s", jsonString);
    }

    @Override
    public Context getMyContext() {
        return this;
    }
}
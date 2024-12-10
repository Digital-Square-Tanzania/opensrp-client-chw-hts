package org.smartregister.chw.hts.custom_views;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.fragment.BaseHtsCallDialogFragment;
import org.smartregister.chw.hts.R;

public class BaseHtsFloatingMenu extends LinearLayout implements View.OnClickListener {
    private MemberObject MEMBER_OBJECT;

    public BaseHtsFloatingMenu(Context context, MemberObject MEMBER_OBJECT) {
        super(context);
        initUi();
        this.MEMBER_OBJECT = MEMBER_OBJECT;
    }

    protected void initUi() {
        inflate(getContext(), R.layout.view_hts_floating_menu, this);
        FloatingActionButton fab = findViewById(R.id.hts_fab);
        if (fab != null)
            fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.hts_fab) {
            Activity activity = (Activity) getContext();
            BaseHtsCallDialogFragment.launchDialog(activity, MEMBER_OBJECT);
        }  else if (view.getId() == R.id.refer_to_facility_layout) {
            Activity activity = (Activity) getContext();
            BaseHtsCallDialogFragment.launchDialog(activity, MEMBER_OBJECT);
        }
    }
}
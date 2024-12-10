package org.smartregister.chw.hts.listener;


import android.view.View;

import org.smartregister.chw.hts.fragment.BaseSkeletonCallDialogFragment;
import org.smartregister.chw.hts.R;

public class BaseSkeletonCallWidgetDialogListener implements View.OnClickListener {

    private BaseSkeletonCallDialogFragment callDialogFragment;

    public BaseSkeletonCallWidgetDialogListener(BaseSkeletonCallDialogFragment dialogFragment) {
        callDialogFragment = dialogFragment;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.hts_call_close) {
            callDialogFragment.dismiss();
        }
    }
}

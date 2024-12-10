package org.smartregister.chw.hts.listener;


import android.view.View;

import org.smartregister.chw.hts.fragment.BaseHtsCallDialogFragment;
import org.smartregister.chw.hts.R;

public class BaseHtsCallWidgetDialogListener implements View.OnClickListener {

    private BaseHtsCallDialogFragment callDialogFragment;

    public BaseHtsCallWidgetDialogListener(BaseHtsCallDialogFragment dialogFragment) {
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

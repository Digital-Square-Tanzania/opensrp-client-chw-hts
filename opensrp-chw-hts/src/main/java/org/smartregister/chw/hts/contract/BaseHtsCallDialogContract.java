package org.smartregister.chw.hts.contract;

import android.content.Context;

public interface BaseHtsCallDialogContract {

    interface View {
        void setPendingCallRequest(Dialer dialer);
        Context getCurrentContext();
    }

    interface Dialer {
        void callMe();
    }
}

package org.smartregister.chw.hts.listener;

import android.app.Activity;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import org.smartregister.listener.BottomNavigationListener;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.chw.hts.R;

public class HtsBottomNavigationListener extends BottomNavigationListener {
    private Activity context;

    public HtsBottomNavigationListener(Activity context) {
        super(context);
        this.context = context;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        super.onNavigationItemSelected(item);

        BaseRegisterActivity baseRegisterActivity = (BaseRegisterActivity) context;

        if (item.getItemId() == R.id.action_home) {
            baseRegisterActivity.switchToBaseFragment();
        }else{
            baseRegisterActivity.switchToFragment(1);
        }

        return true;
    }
}
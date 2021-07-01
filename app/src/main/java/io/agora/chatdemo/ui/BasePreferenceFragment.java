package io.agora.chatdemo.ui;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {

    private void setAllPreferencesToAvoidHavingExtraSpace(Preference preference) {
        preference.setIconSpaceReserved(false);
        if (preference instanceof PreferenceGroup)
            for(int i = 0; i<((PreferenceGroup) preference).getPreferenceCount(); i++){
                setAllPreferencesToAvoidHavingExtraSpace(((PreferenceGroup) preference).getPreference(i));
            }
    }

    @Override
    public void  setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (preferenceScreen != null)
            setAllPreferencesToAvoidHavingExtraSpace(preferenceScreen);
        super.setPreferenceScreen(preferenceScreen);

    }

    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen){
            @Override
            public void onPreferenceHierarchyChange(Preference preference) {
                if(null!=preference){
                    setAllPreferencesToAvoidHavingExtraSpace(preference);
                }
                super.onPreferenceHierarchyChange(preference);
            }
        };
    }
}
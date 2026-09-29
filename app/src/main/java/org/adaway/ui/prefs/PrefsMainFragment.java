package org.adaway.ui.prefs;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.adaway.R;
import org.adaway.helper.PreferenceHelper;
import org.adaway.model.adblocking.AdBlockMethod;

import static org.adaway.model.adblocking.AdBlockMethod.ROOT;
import static org.adaway.ui.prefs.PrefsActivity.PREFERENCE_NOT_FOUND;
import static org.adaway.util.Constants.PREFS_NAME;

/**
 * This fragment is the preferences main fragment.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
public class PrefsMainFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Configure preferences
        getPreferenceManager().setSharedPreferencesName(PREFS_NAME);
        addPreferencesFromResource(R.xml.preferences_main);
        // Bind pref actions
        bindThemePrefAction();
        bindAdBlockMethod();
        bindTelemetryPrefAction();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        PrefsActivity.setAppBarTitle(this, R.string.pref_main_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        PrefsActivity.setAppBarTitle(this, R.string.pref_main_title);
    }

    private void bindThemePrefAction() {
        Preference darkThemePref = findPreference(getString(R.string.pref_dark_theme_mode_key));
        assert darkThemePref != null : PREFERENCE_NOT_FOUND;
        darkThemePref.setOnPreferenceChangeListener((preference, newValue) -> {
            requireActivity().recreate();
            // Allow preference change
            return true;
        });
    }

    private void bindAdBlockMethod() {
        Preference rootPreference = findPreference(getString(R.string.pref_root_ad_block_method_key));
        assert rootPreference != null : PREFERENCE_NOT_FOUND;
        Preference vpnPreference = findPreference(getString(R.string.pref_vpn_ad_block_method_key));
        assert vpnPreference != null : PREFERENCE_NOT_FOUND;
        AdBlockMethod adBlockMethod = PreferenceHelper.getAdBlockMethod(requireContext());
        rootPreference.setEnabled(adBlockMethod == ROOT);
        // The VPN based ad blocker is not supported anymore: keep the option visible but disabled.
        vpnPreference.setEnabled(false);
    }

    private void bindTelemetryPrefAction() {
        // Telemetry (Sentry) has been removed: keep the option visible but disabled.
        Preference enableTelemetryPref = findPreference(getString(R.string.pref_enable_telemetry_key));
        assert enableTelemetryPref != null : PREFERENCE_NOT_FOUND;
        enableTelemetryPref.setEnabled(false);
        enableTelemetryPref.setSummary(R.string.pref_enable_telemetry_disabled_summary);
    }
}

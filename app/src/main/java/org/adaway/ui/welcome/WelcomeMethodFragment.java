package org.adaway.ui.welcome;

import static java.lang.Boolean.TRUE;
import static org.adaway.model.adblocking.AdBlockMethod.ROOT;
import static org.adaway.model.adblocking.AdBlockMethod.UNDEFINED;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import org.adaway.R;
import org.adaway.databinding.WelcomeMethodLayoutBinding;
import org.adaway.helper.PreferenceHelper;

/**
 * This class is a fragment to setup the ad blocking method.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
public class WelcomeMethodFragment extends WelcomeFragment {
    /**
     * Alpha value applied to the disabled VPN based ad-blocking method.
     */
    private static final float DISABLED_ALPHA = 0.5F;

    private WelcomeMethodLayoutBinding binding;
    @ColorInt
    private int cardColor;
    @ColorInt
    private int cardEnabledColor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = WelcomeMethodLayoutBinding.inflate(inflater, container, false);

        // The VPN based ad-blocking method is not supported anymore: gray it out and disable it.
        this.binding.vpnCardView.setAlpha(DISABLED_ALPHA);
        this.binding.vpnMethodLayout.setAlpha(DISABLED_ALPHA);
        this.binding.vpnCardView.setOnClickListener(null);

        this.binding.rootCardView.setOnClickListener(this::checkRoot);

        this.cardColor = getResources().getColor(R.color.cardBackground, null);
        this.cardEnabledColor = getResources().getColor(R.color.cardEnabledBackground, null);
        return this.binding.getRoot();
    }

    private void checkRoot(@Nullable View view) {
        // Reset the selected method while checking
        PreferenceHelper.setAbBlockMethod(requireContext(), UNDEFINED);
        this.binding.rootCardView.setCardBackgroundColor(this.cardColor);
        this.binding.vpnCardView.setCardBackgroundColor(this.cardColor);
        Shell.getShell();
        if (TRUE.equals(Shell.isAppGrantedRoot())) {
            notifyRootEnabled();
        } else {
            notifyRootDisabled(true);
        }
    }

    private void notifyRootEnabled() {
        PreferenceHelper.setAbBlockMethod(requireContext(), ROOT);
        this.binding.rootCardView.setCardBackgroundColor(this.cardEnabledColor);
        this.binding.vpnCardView.setCardBackgroundColor(this.cardColor);
        allowNext();
    }

    private void notifyRootDisabled(boolean showDialog) {
        PreferenceHelper.setAbBlockMethod(requireContext(), UNDEFINED);
        this.binding.rootCardView.setCardBackgroundColor(this.cardColor);
        this.binding.vpnCardView.setCardBackgroundColor(this.cardColor);
        if (showDialog) {
            blockNext();
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.welcome_root_missing_title)
                    .setMessage(R.string.welcome_root_missile_description)
                    .setPositiveButton(R.string.button_close, null)
                    .create()
                    .show();
        }
    }
}

package org.adaway.ui.kpm;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.adaway.R;
import org.adaway.helper.KpmHelper;
import org.adaway.helper.KpmHelper.KpmStatus;
import org.adaway.helper.ThemeHelper;
import org.adaway.util.AppExecutors;

/**
 * This activity displays the status of the KernelPatch Module (KPM) that redirects the hosts file.
 *
 * @author AdAway
 */
public class KpmModuleActivity extends AppCompatActivity {
    private TextView statusTextView;
    private TextView logTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_kpm_module);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.statusTextView = findViewById(R.id.kpm_module_status_text_view);
        this.logTextView = findViewById(R.id.kpm_module_log_text_view);

        checkStatus();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkStatus() {
        this.statusTextView.setText(R.string.kpm_module_loading);
        this.logTextView.setText(null);
        AppExecutors executors = AppExecutors.getInstance();
        executors.diskIO().execute(() -> {
            KpmStatus status = KpmHelper.queryStatus();
            String summary = buildSummary(status);
            executors.mainThread().execute(() -> {
                this.statusTextView.setText(summary);
                this.logTextView.setText(status.rawLog.isEmpty()
                        ? getString(R.string.kpm_module_no_log)
                        : status.rawLog);
            });
        });
    }

    private String buildSummary(KpmStatus status) {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, R.string.kpm_module_root_label,
                getString(status.rootAvailable ? R.string.kpm_module_available : R.string.kpm_module_unavailable));
        appendLine(builder, R.string.kpm_module_loaded_label,
                getString(status.loaded ? R.string.kpm_module_yes : R.string.kpm_module_no));
        appendLine(builder, R.string.kpm_module_status_label,
                getString(status.enabled ? R.string.kpm_module_enabled : R.string.kpm_module_disabled));
        appendLine(builder, R.string.kpm_module_target_label,
                status.redirectTarget.isEmpty() ? getString(R.string.kpm_module_unknown) : status.redirectTarget);

        if (status.name != null) {
            builder.append('\n').append(getString(R.string.kpm_module_info_title)).append('\n');
            appendLine(builder, R.string.kpm_module_name_label, status.name);
            appendLine(builder, R.string.kpm_module_version_label, status.version);
            appendLine(builder, R.string.kpm_module_license_label, status.license);
            appendLine(builder, R.string.kpm_module_author_label, status.author);
            appendLine(builder, R.string.kpm_module_description_label, status.description);
            appendLine(builder, R.string.kpm_module_load_event_label, status.loadEvent);
            appendLine(builder, R.string.kpm_module_load_source_label, status.loadSource);
        }
        return builder.toString().trim();
    }

    private void appendLine(StringBuilder builder, @StringRes int labelResId, String value) {
        builder.append(getString(labelResId)).append(": ");
        if (value == null || value.isEmpty()) {
            builder.append(getString(R.string.kpm_module_unknown));
        } else {
            builder.append(value);
        }
        builder.append('\n');
    }
}

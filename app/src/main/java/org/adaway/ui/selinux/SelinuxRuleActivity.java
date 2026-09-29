package org.adaway.ui.selinux;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.topjohnwu.superuser.Shell;

import org.adaway.R;
import org.adaway.helper.SelinuxRuleHelper;
import org.adaway.helper.ThemeHelper;
import org.adaway.util.AppExecutors;

/**
 * This activity displays the SELinux rules script installed by AdAway to allow the Android
 * resolver ({@code netd}) to read the redirected hosts file in {@code /data/adb}.
 *
 * @author AdAway
 */
public class SelinuxRuleActivity extends AppCompatActivity {
    private TextView statusTextView;
    private TextView contentTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_selinux_rule);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.statusTextView = findViewById(R.id.selinux_rule_status_text_view);
        this.contentTextView = findViewById(R.id.selinux_rule_content_text_view);
        ((TextView) findViewById(R.id.selinux_rule_path_text_view))
                .setText(SelinuxRuleHelper.getScriptPath());

        installAndShow();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void installAndShow() {
        this.statusTextView.setText(R.string.selinux_rule_status_checking);
        this.contentTextView.setText(SelinuxRuleHelper.getScriptContent());
        AppExecutors executors = AppExecutors.getInstance();
        executors.diskIO().execute(() -> {
            boolean installed = SelinuxRuleHelper.ensureInstalled();
            String content = readInstalledScript();
            executors.mainThread().execute(() -> {
                this.statusTextView.setText(
                        installed ? R.string.selinux_rule_status_installed : R.string.selinux_rule_status_failed
                );
                this.contentTextView.setText(content);
            });
        });
    }

    private String readInstalledScript() {
        Shell.Result result = Shell.cmd("cat " + SelinuxRuleHelper.getScriptPath()).exec();
        if (result.isSuccess() && !result.getOut().isEmpty()) {
            return String.join("\n", result.getOut());
        }
        return SelinuxRuleHelper.getScriptContent();
    }
}

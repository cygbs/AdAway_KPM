package org.adaway.helper;

import static java.util.regex.Pattern.quote;

import com.topjohnwu.superuser.Shell;

import java.util.List;

/**
 * This class is an helper to query the status of the KernelPatch Module (KPM) that redirects the
 * hosts file ({@code hosts_file_redirect}).
 * <p>
 * The detection is based on the actual hook behavior: a root process opening
 * {@code /system/etc/hosts} gets redirected to {@code /data/adb/hosts} when the module is enabled.
 * The kernel log is also read to report whether the module is loaded and to expose its metadata,
 * even when the module is currently disabled.
 *
 * @author AdAway
 */
public final class KpmHelper {
    /**
     * The hosts redirection KPM name.
     */
    public static final String MODULE_NAME = "hosts_file_redirect";
    /**
     * The redirect target of the enabled module.
     */
    public static final String REDIRECT_TARGET = "/data/adb/hosts";

    /**
     * Private constructor.
     */
    private KpmHelper() {
    }

    /**
     * Query the KPM status.
     *
     * @return The KPM status.
     */
    public static KpmStatus queryStatus() {
        KpmStatus status = new KpmStatus();
        Shell.Result result = Shell.cmd(
                "echo KPM_PROBE_BEGIN",
                "if [ -r /system/etc/hosts ]; then exec 9</system/etc/hosts; readlink /proc/$$/fd/9; exec 9<&-; fi",
                "echo KPM_PROBE_END",
                "echo KPM_LOG_BEGIN",
                "grep -ahE 'hosts_file_redirect|HFR:|KP load kpm' /data/adb/ap/log/dmesg.log /data/adb/ap/log/dmesg.last 2>/dev/null",
                "echo KPM_LOG_END",
                "echo KPM_DMESG_BEGIN",
                "dmesg 2>/dev/null | grep -aE 'hosts_file_redirect|HFR:'",
                "echo KPM_DMESG_END"
        ).exec();

        status.rootAvailable = result.isSuccess();
        List<String> out = result.getOut();
        status.redirectTarget = section(out, "KPM_PROBE_BEGIN", "KPM_PROBE_END").trim();
        status.enabled = status.rootAvailable && REDIRECT_TARGET.equals(status.redirectTarget);

        String log = section(out, "KPM_LOG_BEGIN", "KPM_LOG_END");
        String dmesg = section(out, "KPM_DMESG_BEGIN", "KPM_DMESG_END");
        StringBuilder raw = new StringBuilder();
        if (!log.isBlank()) {
            raw.append(log);
        }
        if (!dmesg.isBlank()) {
            if (raw.length() > 0) {
                raw.append('\n');
            }
            raw.append(dmesg);
        }
        status.rawLog = raw.toString().trim();

        parseModuleInfo(status, status.rawLog);
        status.loaded = status.enabled
                || status.name != null
                || status.rawLog.contains("KP load kpm: " + MODULE_NAME);
        return status;
    }

    private static String section(List<String> lines, String begin, String end) {
        StringBuilder builder = new StringBuilder();
        boolean inside = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (begin.equals(trimmed)) {
                inside = true;
            } else if (end.equals(trimmed)) {
                inside = false;
            } else if (inside) {
                builder.append(line).append('\n');
            }
        }
        return builder.toString();
    }

    private static void parseModuleInfo(KpmStatus status, String log) {
        String prefix = "get_module_info: ";
        int index = log.indexOf(prefix);
        if (index < 0) {
            return;
        }
        int start = index + prefix.length();
        int end = log.indexOf('\n', start);
        String line = end < 0 ? log.substring(start) : log.substring(start, end);
        for (String part : line.split(quote("\\x0a"))) {
            int separator = part.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = part.substring(0, separator).trim();
            String value = part.substring(separator + 1).trim();
            switch (key) {
                case "name":
                    status.name = value;
                    break;
                case "version":
                    status.version = value;
                    break;
                case "license":
                    status.license = value;
                    break;
                case "author":
                    status.author = value;
                    break;
                case "description":
                    status.description = value;
                    break;
                case "load_event":
                    status.loadEvent = value;
                    break;
                case "load_source":
                    status.loadSource = value;
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * This class represents the KPM status.
     */
    public static class KpmStatus {
        /**
         * Whether root access is available.
         */
        public boolean rootAvailable;
        /**
         * Whether the KPM is loaded in the kernel.
         */
        public boolean loaded;
        /**
         * Whether the KPM redirection is currently enabled.
         */
        public boolean enabled;
        /**
         * The file the root open of {@code /system/etc/hosts} actually resolves to.
         */
        public String redirectTarget = "";
        /**
         * The module name.
         */
        public String name;
        /**
         * The module version.
         */
        public String version;
        /**
         * The module license.
         */
        public String license;
        /**
         * The module author.
         */
        public String author;
        /**
         * The module description.
         */
        public String description;
        /**
         * The module load event.
         */
        public String loadEvent;
        /**
         * The module load source.
         */
        public String loadSource;
        /**
         * The raw kernel log evidence.
         */
        public String rawLog = "";
    }
}

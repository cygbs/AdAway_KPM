package org.adaway.model.root;

import android.content.Context;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.List;

/**
 * This class is an utility class to help with shell commands.
 *
 * @author Bruce BUJON (bruce.bujon(at)gmail(dot)com)
 */
public final class ShellUtils {
    private static final String EXECUTABLE_PREFIX = "lib";
    private static final String EXECUTABLE_SUFFIX = "_exec.so";

    /**
     * Private constructor.
     */
    private ShellUtils() {

    }

    public static String mergeAllLines(List<String> lines) {
        return String.join("\n", lines);
    }

    public static boolean isBundledExecutableRunning(String executable) {
        return Shell.cmd("ps -A | grep " + EXECUTABLE_PREFIX + executable + EXECUTABLE_SUFFIX).exec().isSuccess();
    }

    public static boolean runBundledExecutable(Context context, String executable, String parameters) {
        String nativeLibraryDir = context.getApplicationInfo().nativeLibraryDir;
        String command = "LD_LIBRARY_PATH=" + nativeLibraryDir + " " +
                nativeLibraryDir + File.separator + EXECUTABLE_PREFIX + executable + EXECUTABLE_SUFFIX + " " +
                parameters + " & " +
                // Detach the daemon from the application freezer cgroup: otherwise Android freezes
                // it together with the cached application and it stops answering (e.g. web server).
                "echo $! > /dev/cpufreezer/cgroup.procs 2>/dev/null || " +
                "echo $! > /sys/fs/cgroup/cgroup.procs 2>/dev/null";
        return Shell.cmd(command).exec().isSuccess();
    }

    public static void killBundledExecutable(String executable) {
        Shell.cmd("killall " + EXECUTABLE_PREFIX + executable + EXECUTABLE_SUFFIX).exec();
    }
}

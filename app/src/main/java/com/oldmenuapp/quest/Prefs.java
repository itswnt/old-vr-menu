package com.oldmenuapp.quest;

import android.content.Context;
import android.content.SharedPreferences;

final class Prefs {
    static final String PKG = "com.oculus.systemux";
    static final String CLS = "com.oculus.panelapp.anytimeui.AnytimeUIActivity";
    static final int MAX_FAILS = 3;

    static SharedPreferences sp(Context c) { return c.getSharedPreferences("p", Context.MODE_PRIVATE); }
    /** Old menu switch. On by default. */
    static boolean enabled(Context c) { return sp(c).getBoolean("enabled", true); }
    static void setEnabled(Context c, boolean v) {
        sp(c).edit().putBoolean("enabled", v).putInt("fails", 0).apply();
    }
    /** Startup after restart: 0 = remember last choice, 1 = always on, 2 = always off */
    static int bootMode(Context c) { return sp(c).getInt("bootMode", 0); }
    /** Quick close shortcut (A + X). On by default. */
    static boolean quickClose(Context c) { return sp(c).getBoolean("quickClose", true); }
    /** Quick uninstall shortcut (menu button x3). On by default. */
    static boolean quickUninstall(Context c) { return sp(c).getBoolean("quickUninstall", true); }
}

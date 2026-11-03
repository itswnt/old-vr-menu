package com.oldmenuapp.quest;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class MenuService extends AccessibilityService {
    static MenuService instance;
    private long lastLaunch = 0, aDown = 0, xDown = 0;
    private final long[] menuTaps = new long[3];
    private int tapIdx = 0;

    @Override protected void onServiceConnected() {
        instance = this;
        int m = Prefs.bootMode(this);                      // runs each time the headset starts
        if (m == 1) Prefs.setEnabled(this, true);
        else if (m == 2) Prefs.setEnabled(this, false);
    }
    @Override public void onDestroy() { instance = null; super.onDestroy(); }

    @Override public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        String pkg = String.valueOf(e.getPackageName());
        String cls = String.valueOf(e.getClassName());
        Prefs.sp(this).edit().putString("last", pkg + "\n" + cls).apply();

        if (!Prefs.enabled(this)) return;
        if (Prefs.sp(this).getInt("fails", 0) >= Prefs.MAX_FAILS) return;
        if (cls.equals(Prefs.CLS)) return;               // already on the old menu
        if (!cls.contains("HomeActivity")) return;       // only react to the new home menu
        long now = System.currentTimeMillis();
        if (now - lastLaunch < 3000) return;             // loop guard
        lastLaunch = now;
        try {
            startActivity(new Intent().setComponent(new ComponentName(Prefs.PKG, Prefs.CLS))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception ex) {
            Prefs.sp(this).edit().putInt("fails", Prefs.sp(this).getInt("fails", 0) + 1).apply();
        }
    }

    // Quick close: A + X together within 2 s.   Quick uninstall: menu (three lines) button 3x within 2 s.
    // Best effort: depends on Quest passing controller buttons to accessibility services.
    @Override protected boolean onKeyEvent(KeyEvent k) {
        if (k.getAction() != KeyEvent.ACTION_DOWN) return false;
        long now = System.currentTimeMillis();
        int c = k.getKeyCode();
        if (c == KeyEvent.KEYCODE_BUTTON_A) aDown = now;
        else if (c == KeyEvent.KEYCODE_BUTTON_X) xDown = now;
        else if (c == KeyEvent.KEYCODE_BUTTON_START || c == KeyEvent.KEYCODE_MENU) {
            menuTaps[tapIdx++ % 3] = now;
            long oldest = Math.min(menuTaps[0], Math.min(menuTaps[1], menuTaps[2]));
            if (oldest > 0 && now - oldest < 2000 && Prefs.quickUninstall(this)) quickUninstall();
            return false;
        }
        if (aDown > 0 && xDown > 0 && Math.abs(aDown - xDown) < 2000 && Prefs.quickClose(this)) quickClose();
        return false;                                       // never swallow a button press
    }

    void quickClose() {
        aDown = xDown = 0;
        Prefs.setEnabled(this, false);
        Toast.makeText(this, "Old Menu closed", Toast.LENGTH_LONG).show();
        disableSelf();
    }

    void quickUninstall() {
        java.util.Arrays.fill(menuTaps, 0);
        Prefs.setEnabled(this, false);
        Toast.makeText(this, "Old Menu: uninstall prompt", Toast.LENGTH_LONG).show();
        startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + getPackageName()))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        disableSelf();
    }
    @Override public void onInterrupt() {}
}

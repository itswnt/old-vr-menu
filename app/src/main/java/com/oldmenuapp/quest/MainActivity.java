package com.oldmenuapp.quest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    static final int BG = Color.rgb(14, 16, 20), CARD = Color.rgb(28, 31, 38),
            TEXT = Color.rgb(236, 238, 242), DIM = Color.rgb(150, 156, 168),
            ACCENT = Color.rgb(86, 156, 214), WARN = Color.rgb(232, 168, 56),
            GREEN = Color.rgb(64, 200, 110), RED = Color.rgb(224, 74, 74),
            GREEN_TRACK = Color.rgb(30, 90, 56), RED_TRACK = Color.rgb(98, 36, 36);

    private Switch mainSwitch; private TextView mainState, status, last;
    private ScrollView settingsView;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        settingsView = buildSettings();
        if (Prefs.sp(this).getBoolean("warned", false)) setContentView(settingsView);
        else setContentView(buildWarning());
    }

    // ---------- warning screen ----------
    private View buildWarning() {
        LinearLayout r = column(80);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.addView(text("Before you continue", 36, TEXT, true));
        LinearLayout c = card(WARN);
        c.addView(text("Unofficial app — may have bugs", 24, WARN, true));
        c.addView(text("Old Menu is not made, approved or supported by Meta.\n\n"
            + "• The old menu may not open, or may look different after a system update.\n"
            + "• You might see the menu flicker or open twice.\n"
            + "• If something goes wrong, turn the switch off or uninstall the app. Nothing on the headset is changed permanently.\n"
            + "• This app has no internet access and doesn't read your account.", 19, TEXT, false, 0, 20));
        r.addView(c, lp(40));
        CheckBox cb = new CheckBox(this);
        cb.setText("I understand, don't show this again"); cb.setTextColor(DIM); cb.setTextSize(18);
        r.addView(cb, lp(30));
        LinearLayout row = new LinearLayout(this);
        row.addView(button("Exit", false, v -> finish()), weight());
        row.addView(button("Launch anyway", true, v -> {
            Prefs.sp(this).edit().putBoolean("warned", cb.isChecked()).apply();
            setContentView(settingsView); refresh();
        }), weight());
        r.addView(row, lp(30));
        return r;
    }

    // ---------- settings screen ----------
    private ScrollView buildSettings() {
        ScrollView sv = new ScrollView(this); sv.setBackgroundColor(BG);
        LinearLayout r = column(70); sv.addView(r);
        r.addView(text("Menus", 40, TEXT, true));
        r.addView(text("Choose which menu you want on your Quest", 18, DIM, false, 0, 6));

        // main toggle
        LinearLayout m = card(0);
        mainState = text("", 18, GREEN, true);
        mainSwitch = makeSwitch(Prefs.enabled(this), (v, on) -> { Prefs.setEnabled(this, on); refresh(); });
        m.addView(toggleRow("Use old menu (2023 style)",
                "When on, opening the home menu takes you to the classic library grid.", mainState, mainSwitch));
        r.addView(m, lp(40));

        // setup
        LinearLayout s = card(0);
        s.addView(text("Setup", 22, TEXT, true));
        s.addView(text("The switch only works while the accessibility service is on.", 17, DIM, false, 0, 8));
        status = text("", 18, WARN, true, 0, 14); s.addView(status);
        s.addView(button("Open accessibility settings", true, v ->
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))), lp(16));
        s.addView(button("Open old menu now (test)", false, v -> {
            try { startActivity(new Intent().setComponent(new ComponentName(Prefs.PKG, Prefs.CLS))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
            catch (Exception e) { Toast.makeText(this, "Not available on this system version", Toast.LENGTH_LONG).show(); }
        }), lp(12));
        r.addView(s, lp(24));

        // startup
        LinearLayout st = card(0);
        st.addView(text("Startup", 22, TEXT, true));
        st.addView(text("What happens when the headset restarts", 17, DIM, false, 0, 8));
        RadioGroup rg = new RadioGroup(this);
        String[] names = {"Remember my last choice", "Always start ON", "Always start OFF"};
        for (int i = 0; i < 3; i++) {
            RadioButton rb = new RadioButton(this); rb.setText(names[i]); rb.setId(100 + i);
            rb.setTextColor(TEXT); rb.setTextSize(19); rg.addView(rb);
        }
        rg.check(100 + Prefs.bootMode(this));
        rg.setOnCheckedChangeListener((g, id) -> Prefs.sp(this).edit().putInt("bootMode", id - 100).apply());
        st.addView(rg);
        r.addView(st, lp(24));

        // shortcuts: two separate switches
        LinearLayout sh = card(0);
        sh.addView(text("Shortcuts", 22, TEXT, true));
        sh.addView(prefToggle("Quick close shortcut",
                "Press A + X together within 2 seconds to turn the old menu off.", "quickClose"), lp(18));
        sh.addView(prefToggle("Quick uninstall shortcut",
                "Tap the menu (three lines) button 3 times within 2 seconds to open the uninstall prompt.", "quickUninstall"), lp(22));
        sh.addView(text("Shortcuts may not work on every system version.", 15, DIM, false, 0, 14));
        r.addView(sh, lp(24));

        // buttons
        LinearLayout act = card(0);
        act.addView(text("Actions", 22, TEXT, true));
        act.addView(button("Quick close (stop now)", false, v -> {
            Prefs.setEnabled(this, false); if (MenuService.instance != null) MenuService.instance.disableSelf(); refresh();
        }), lp(14));
        act.addView(button("Quick uninstall", false, v ->
            startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + getPackageName())))), lp(12));
        r.addView(act, lp(24));

        // debug
        LinearLayout d = card(0);
        d.addView(text("Debug info", 22, TEXT, true));
        last = text("", 15, DIM, false, 0, 8); d.addView(last);
        d.addView(text("Known issues: may break after Meta updates. Unofficial.", 15, DIM, false, 0, 8));
        r.addView(d, lp(24));
        return sv;
    }

    @Override protected void onResume() { super.onResume(); if (mainSwitch != null) refresh(); }

    private void refresh() {
        boolean en = Prefs.enabled(this);
        mainSwitch.setOnCheckedChangeListener(null);
        mainSwitch.setChecked(en);
        mainSwitch.setOnCheckedChangeListener((v, on) -> { Prefs.setEnabled(this, on); refresh(); });
        setState(mainState, en);
        String svc = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        boolean svcOn = svc != null && svc.contains(getPackageName());
        int fails = Prefs.sp(this).getInt("fails", 0);
        status.setTextColor(svcOn ? GREEN : WARN);
        status.setText(fails >= Prefs.MAX_FAILS ? "Stopped after failed launches. Toggle the switch to retry."
                : svcOn ? "Service is on" : "Service is off — turn it on below");
        last.setText("Last window seen:\n" + Prefs.sp(this).getString("last", "(none yet)"));
    }

    // ---------- UI helpers ----------
    private void setState(TextView t, boolean on) {
        t.setText(on ? "ON" : "OFF"); t.setTextColor(on ? GREEN : RED);
    }
    /** Green when on, red when off. */
    private Switch makeSwitch(boolean checked, CompoundButton.OnCheckedChangeListener l) {
        Switch s = new Switch(this);
        int[][] states = {{android.R.attr.state_checked}, {}};
        s.setThumbTintList(new ColorStateList(states, new int[]{GREEN, RED}));
        s.setTrackTintList(new ColorStateList(states, new int[]{GREEN_TRACK, RED_TRACK}));
        s.setScaleX(1.4f); s.setScaleY(1.4f);
        s.setChecked(checked); s.setOnCheckedChangeListener(l);
        return s;
    }
    private View toggleRow(String title, String desc, TextView state, Switch sw) {
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout left = new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL);
        left.addView(text(title, 22, TEXT, true));
        left.addView(text(desc, 16, DIM, false, 0, 4));
        row.addView(left, new LinearLayout.LayoutParams(0, -2, 1));
        state.setPadding(20, 0, 30, 0);
        row.addView(state); row.addView(sw);
        return row;
    }
    private View prefToggle(String title, String desc, String key) {
        TextView state = text("", 18, GREEN, true);
        boolean on = Prefs.sp(this).getBoolean(key, true);
        setState(state, on);
        Switch sw = makeSwitch(on, (v, c) -> { Prefs.sp(this).edit().putBoolean(key, c).apply(); setState(state, c); });
        return toggleRow(title, desc, state, sw);
    }
    private LinearLayout column(int pad) {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(BG); l.setPadding(pad, pad, pad, pad); return l;
    }
    private LinearLayout card(int stripe) {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(40, 32, 40, 32);
        GradientDrawable g = new GradientDrawable(); g.setColor(CARD); g.setCornerRadius(28);
        if (stripe != 0) g.setStroke(3, stripe);
        l.setBackground(g); return l;
    }
    private TextView text(String s, int sp, int color, boolean bold) { return text(s, sp, color, bold, 0, 0); }
    private TextView text(String s, int sp, int color, boolean bold, int l, int top) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD); t.setPadding(l, top, 0, 0); return t;
    }
    private Button button(String s, boolean primary, View.OnClickListener c) {
        Button b = new Button(this); b.setText(s); b.setAllCaps(false); b.setTextSize(18);
        b.setTextColor(primary ? Color.WHITE : TEXT);
        GradientDrawable g = new GradientDrawable(); g.setCornerRadius(60);
        g.setColor(primary ? ACCENT : Color.rgb(48, 52, 62)); b.setBackground(g);
        b.setOnClickListener(c); return b;
    }
    private LinearLayout.LayoutParams lp(int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.topMargin = top; return p;
    }
    private LinearLayout.LayoutParams weight() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1); p.setMargins(8, 0, 8, 0); return p;
    }
}

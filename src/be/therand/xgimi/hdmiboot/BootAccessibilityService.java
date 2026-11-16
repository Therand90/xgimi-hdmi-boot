package be.therand.xgimi.hdmiboot;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.media.tv.TvInputManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * XGIMI boot helper and optional guarded HDMI1 source-popup dismissal.
 *
 * Requires BOTH an HDMI1 connection event and an XHPlayer XGMDialog state
 * change in a short window. No window content, text, or screenshots read.
 * The user may disable popup dismissal in the app settings.
 */
public final class BootAccessibilityService extends AccessibilityService {
    private static final String XGIMI_PLAYER = "com.xgimi.xhplayer";
    private static final String XGIMI_DIALOG = "com.xgimi.dialog.XGMDialog";
    private static final String HDMI1_MARKER = "Hdmi1TvInputService/HW1";
    private static final long SIGNAL_WINDOW_MS = 4000L;
    private static final long MIN_BACK_INTERVAL_MS = 15000L;

    private TvInputManager tvInputManager;
    private TvInputManager.TvInputCallback inputCallback;
    private long lastHdmi1ConnectMs = -SIGNAL_WINDOW_MS - 1;
    private long lastBackAttemptMs = -MIN_BACK_INTERVAL_MS - 1;

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(HdmiSwitch.TAG, "Accessibility helper connected: bootstrapping wake listener");
        registerHdmi1Observer();
        if (!pref("enabled", true)) {
            Log.i(HdmiSwitch.TAG, "Accessibility helper: automation disabled");
            return;
        }
        try {
            WakeService.start(this);
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && pm.isInteractive()) {
                SwitchReceiver.schedule(this, 6000);
            } else {
                Log.i(HdmiSwitch.TAG,
                    "Accessibility helper: screen not interactive; no HDMI switch now");
            }
        } catch (RuntimeException e) {
            Log.e(HdmiSwitch.TAG, "Accessibility helper bootstrap failed", e);
        }
    }

    private boolean pref(String key, boolean fallback) {
        return getSharedPreferences("config", Context.MODE_PRIVATE)
            .getBoolean(key, fallback);
    }

    private void registerHdmi1Observer() {
        if (tvInputManager != null) return;
        try {
            TvInputManager manager = (TvInputManager) getSystemService(Context.TV_INPUT_SERVICE);
            if (manager == null) {
                Log.w(HdmiSwitch.TAG, "POPUP_AUTOCLOSE: TvInputManager unavailable");
                return;
            }
            TvInputManager.TvInputCallback callback = new TvInputManager.TvInputCallback() {
                @Override public void onInputStateChanged(String inputId, int state) {
                    if (inputId == null || !inputId.contains(HDMI1_MARKER)
                        || state != TvInputManager.INPUT_STATE_CONNECTED) return;
                    if (!pref("enabled", true) || !pref("popup_autoclose", true)) return;
                    lastHdmi1ConnectMs = SystemClock.uptimeMillis();
                    Log.i(HdmiSwitch.TAG,
                        "POPUP_AUTOCLOSE: HDMI1 connected; awaiting XGMDialog for 4s");
                }
            };
            manager.registerCallback(callback, new Handler(Looper.getMainLooper()));
            tvInputManager = manager;
            inputCallback = callback;
            Log.i(HdmiSwitch.TAG, "POPUP_AUTOCLOSE: HDMI1 input observer registered");
        } catch (RuntimeException e) {
            Log.w(HdmiSwitch.TAG, "POPUP_AUTOCLOSE: HDMI1 observer unavailable", e);
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null
            || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            || event.getPackageName() == null
            || !XGIMI_PLAYER.contentEquals(event.getPackageName())) return;

        CharSequence cls = event.getClassName();
        if (!pref("enabled", true) || !pref("popup_autoclose", true)
            || cls == null || !XGIMI_DIALOG.contentEquals(cls)) return;
        long now = SystemClock.uptimeMillis();
        long elapsed = now - lastHdmi1ConnectMs;
        if (elapsed < 0 || elapsed > SIGNAL_WINDOW_MS) {
            Log.i(HdmiSwitch.TAG,
                "POPUP_AUTOCLOSE: XGMDialog ignored (no recent HDMI1 connection)");
            return;
        }
        if (now - lastBackAttemptMs < MIN_BACK_INTERVAL_MS) {
            Log.i(HdmiSwitch.TAG, "POPUP_AUTOCLOSE: rate-limited; no BACK");
            return;
        }
        // Consume the HDMI1 trigger before injecting BACK to avoid recursive
        // window-state events triggering a second action.
        lastHdmi1ConnectMs = -SIGNAL_WINDOW_MS - 1;
        lastBackAttemptMs = now;
        boolean accepted = performGlobalAction(GLOBAL_ACTION_BACK);
        Log.i(HdmiSwitch.TAG,
            "POPUP_AUTOCLOSE: guarded BACK " + (accepted ? "accepted" : "rejected"));
    }

    @Override public void onInterrupt() { }

    @Override public void onDestroy() {
        if (tvInputManager != null && inputCallback != null) {
            try {
                tvInputManager.unregisterCallback(inputCallback);
            } catch (RuntimeException e) {
                Log.w(HdmiSwitch.TAG, "POPUP_AUTOCLOSE: observer cleanup failed", e);
            }
        }
        tvInputManager = null;
        inputCallback = null;
        super.onDestroy();
    }
}

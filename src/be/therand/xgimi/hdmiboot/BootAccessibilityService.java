package be.therand.xgimi.hdmiboot;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Optional startup bootstrap for XGIMI firmware which sets ordinary apps to
 * stopped=true on boot. Opt-in by the device user in Accessibility settings.
 *
 * Popup diagnostic (explicit opt-in only): watches XGIMI window-state metadata.
 * Never reads window contents, sends keys, or performs accessibility gestures.
 */
public final class BootAccessibilityService extends AccessibilityService {
    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(HdmiSwitch.TAG, "Accessibility helper connected: bootstrapping wake listener");
        if (!getSharedPreferences("config", Context.MODE_PRIVATE).getBoolean("enabled", true)) {
            Log.i(HdmiSwitch.TAG, "Accessibility helper: automation disabled");
            return;
        }
        try {
            WakeService.start(this);
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && pm.isInteractive()) {
                SwitchReceiver.schedule(this, 6000);
            } else {
                Log.i(HdmiSwitch.TAG, "Accessibility helper: screen not interactive; no HDMI switch now");
            }
        } catch (RuntimeException e) {
            Log.e(HdmiSwitch.TAG, "Accessibility helper bootstrap failed", e);
        }
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        // This is an opt-in, metadata-only diagnostic. No content, text,
        // view tree, screenshot, key, or gesture is ever accessed.
        if (event == null
            || !getSharedPreferences("config", Context.MODE_PRIVATE)
                .getBoolean("popup_probe", false)
            || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            || event.getPackageName() == null
            || !"com.xgimi.xhplayer".contentEquals(event.getPackageName())) {
            return;
        }
        CharSequence cls = event.getClassName();
        Log.i(HdmiSwitch.TAG, "POPUP_PROBE window_state class="
            + (cls == null ? "(unknown)" : cls.toString())
            + " windowId=" + event.getWindowId()
            + " eventTime=" + event.getEventTime());
    }
    @Override public void onInterrupt() { }
}

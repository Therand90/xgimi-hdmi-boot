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
 * No accessibility events, screen contents, or gestures are requested.
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
        // No events are requested or processed.
    }
    @Override public void onInterrupt() { }
}

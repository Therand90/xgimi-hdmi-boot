# XGIMI HDMI Boot v1.2 — hardware validation record

Tested hardware: **original XGIMI Horizon XK03H**, Android TV 11, firmware RT.3.24.327.

## Root cause resolved

On the projector firmware, a normal third-party application could be left with `stopped=true` after a full Android reboot. The declared `BOOT_COMPLETED` receiver was not enough to relaunch `WakeService`, and no HDMI switch happened until the app was opened manually.

The user explicitly enabled **XGIMI HDMI Boot (autostart)** through Android TV's Accessibility menu. This opt-in, event-free helper is able to reconnect after reboot and restart the foreground wake listener.

After the complete device reboot, the projector automatically switched to HDMI 2. The ADB diagnosis **before opening the app manually** showed:

```text
stopped=false
Bound services: Service[label=XGIMI HDMI Boot (autost...)]
WakeService ... isForeground=true
```

The original boot-helper delay of 15 seconds worked; the shorter **6-second delay** also successfully switched HDMI 2. Normal screen-on handling still uses **4 seconds**.

## Permissions and setup path

**Accessibility via remote:** confirmed by the user. On the original Horizon go to:

`Settings → Device Preferences → Accessibility → XGIMI HDMI Boot (autostart) → Enable`.

The user confirmed they could **disable and re-enable** the service with the remote. Android's generic Accessibility Settings shortcut and one XGIMI OEM activity could not open the same menu programmatically. The app therefore displays this manual path when the helper is not enabled.

**Overlay (display over other apps):** its system menu was found on the projector under:

`Settings → Apps → Special app access → Display over other apps → XGIMI HDMI Boot`.

The app includes a **Manage overlay permission** button, with an app-details fallback; this button's behavior on the original device has not yet been explicitly confirmed. The tested setup's overlay authorization was granted via the ADB install script. **A wholly ADB-free fresh-install permission flow is not yet confirmed**.

The Accessibility service requests no Accessibility events, no screen content and no gestures, but users must knowingly opt in because Accessibility remains a sensitive system permission.

## Public v1.2 changes

- Full Android reboot HDMI 2 recovery after 6-second post-bootstrap delay.
- Wake from standby HDMI 2 after 4 seconds.
- Simplified, scrollable TV UI: one automation toggle, a functional permission management intent with fallback, permission statuses, and brief Accessibility directions only when needed.
- Deleted diagnostic buttons and the nonfunctional Accessibility Settings shortcut.
- No changes to HDMI-CEC / HDMI 1 ARC settings.

## Release signing

The official APK must be signed with the **same private maintainer certificate as v1.1**. `PUBLISH_v1.2.sh` validates that certificate against the APK attached to the v1.1 GitHub Release *before* it publishes the v1.2 APK. GitHub Actions temporary APKs are only for verification and are **not** compatible public update assets.

package be.therand.xgimi.hdmiboot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class SetupActivity extends Activity {
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(20, 23, 29));

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(40, 16, 40, 16);
        scroll.addView(root);
        setContentView(scroll);

        // Drop the v1.2 experimental-only diagnostic flag on upgrade.
        getSharedPreferences("config", MODE_PRIVATE)
            .edit().remove("popup_probe").apply();

        // Opening the app manually also recovers wake listening in v1.x.
        if (isEnabled()) startWakeService();
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (root != null) render();
    }

    private void render() {
        root.removeAllViews();

        addText("XGIMI HDMI Boot", 28, Color.WHITE);
        addText("Horizon XK03H  |  HDMI 2", 17, 0xffdddddd);

        final boolean enabled = isEnabled();
        addText("Commutation au reveil : " + (enabled ? "ACTIVEE" : "DESACTIVEE"),
            18, enabled ? 0xff99dd99 : 0xffffbf77);

        boolean overlay = Settings.canDrawOverlays(this);
        addText("Autorisation de superposition : " + (overlay ? "OK" : "A CONFIGURER"),
            16, overlay ? 0xff99dd99 : 0xffffbf77);

        boolean bootHelper = isBootHelperEnabled();
        addText("Demarrage apres reboot : " +
                (bootHelper ? "ACCESSIBILITE ACTIVEE" : "ACCESSIBILITE NON ACTIVEE"),
            16, bootHelper ? 0xff99dd99 : 0xffffbf77);
        if (!bootHelper) {
            addText("Pour activer le demarrage automatique apres un redemarrage du projecteur, merci d'aller dans : Parametres > Preferences relatives a l'appareil > Accessibilite > XGIMI HDMI Boot (autostart) > Activer.",
                14, 0xffffbf77);
        }

        final boolean popupAutoclose = getSharedPreferences("config", MODE_PRIVATE)
            .getBoolean("popup_autoclose", true);
        addText("Fermeture popup HDMI 1 : "
                + (popupAutoclose ? "ACTIVEE" : "DESACTIVEE"),
            16, popupAutoclose ? 0xff99dd99 : 0xffbbbbbb);

        Button closePopupButton = new Button(this);
        closePopupButton.setText(popupAutoclose
            ? "Desactiver la fermeture popup HDMI 1"
            : "Activer la fermeture popup HDMI 1");
        closePopupButton.setAllCaps(false);
        closePopupButton.setTextSize(16);
        closePopupButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                boolean next = !getSharedPreferences("config", MODE_PRIVATE)
                    .getBoolean("popup_autoclose", true);
                getSharedPreferences("config", MODE_PRIVATE)
                    .edit().putBoolean("popup_autoclose", next).apply();
                Toast.makeText(SetupActivity.this,
                    next ? "Fermeture popup activee"
                         : "Fermeture automatique desactivee",
                    Toast.LENGTH_SHORT).show();
                render();
            }
        });
        LinearLayout.LayoutParams closePopupParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        closePopupParams.topMargin = 8;
        root.addView(closePopupButton, closePopupParams);

        addText("Ferme les propositions de source lorsqu'un appareil branche "
                + "sur HDMI 1 est detecte. Une notification XGIMI ouverte pendant "
                + "la detection pourrait aussi etre fermee. Vous pouvez desactiver "
                + "cette fonction a tout moment.",
            14, 0xffbbbbbb);

        addText("HDMI-CEC : si un appareil raccorde a un autre port impose sa "
                + "source a l'allumage, desactiver uniquement sa fonction "
                + "'Lecture en une touche' / 'One Touch Play' (nom selon la marque). "
                + "Conserver HDMI-CEC et ARC/eARC actives.",
            14, 0xffbbbbbb);

        Button toggle = new Button(this);
        toggle.setText(enabled ? "Desactiver HDMI 2 automatique" : "Activer HDMI 2 automatique");
        toggle.setAllCaps(false);
        toggle.setTextSize(18);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                boolean next = !isEnabled();
                getSharedPreferences("config", MODE_PRIVATE)
                    .edit().putBoolean("enabled", next).apply();
                if (next) {
                    startWakeService();
                    Toast.makeText(SetupActivity.this, "HDMI 2 automatique active",
                        Toast.LENGTH_SHORT).show();
                } else {
                    SwitchReceiver.cancel(SetupActivity.this);
                    stopService(new Intent(SetupActivity.this, WakeService.class));
                    Toast.makeText(SetupActivity.this, "HDMI 2 automatique desactive",
                        Toast.LENGTH_SHORT).show();
                }
                render();
            }
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = 14;
        root.addView(toggle, buttonParams);

        Button overlayButton = new Button(this);
        overlayButton.setText("Gerer l'autorisation de superposition");
        overlayButton.setAllCaps(false);
        overlayButton.setTextSize(18);
        overlayButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent settings = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                try {
                    startActivity(settings);
                } catch (RuntimeException e) {
                    // Some Android TV firmwares hide the special-access activity.
                    try {
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + getPackageName())));
                    } catch (RuntimeException nested) {
                        Toast.makeText(SetupActivity.this,
                            "Ouvrez Parametres > Applications > Acces special > Afficher par-dessus les autres applications",
                            Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        LinearLayout.LayoutParams overlayButtonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        overlayButtonParams.topMargin = 8;
        root.addView(overlayButton, overlayButtonParams);

        if (!overlay) {
            addText("L'autorisation 'Afficher par-dessus les autres applications' manque. Elle est indispensable a cette version pour commuter depuis l'arriere-plan.",
                14, 0xffffbf77);
            addText("Chemin manuel : Parametres > Applications > Acces special aux applications > Afficher par-dessus les autres applications > XGIMI HDMI Boot.",
                14, 0xffbbbbbb);
        }
    }

    private boolean isEnabled() {
        return getSharedPreferences("config", MODE_PRIVATE).getBoolean("enabled", true);
    }

    private boolean isBootHelperEnabled() {
        try {
            String list = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            String prefix = getPackageName() + "/";
            return list != null && (
                (":" + list + ":").contains(":" + prefix +
                    "be.therand.xgimi.hdmiboot.BootAccessibilityService:")
                || (":" + list + ":").contains(":" + prefix +
                    ".BootAccessibilityService:")
            );
        } catch (Exception e) {
            return false;
        }
    }

    private void startWakeService() {
        try {
            WakeService.start(this);
        } catch (RuntimeException e) {
            android.util.Log.e(HdmiSwitch.TAG, "Service non demarre", e);
            Toast.makeText(this, "Impossible de lancer le service HDMI", Toast.LENGTH_LONG).show();
        }
    }

    private void addText(String message, int sizeSp, int color) {
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextSize(sizeSp);
        text.setTextColor(color);
        text.setPadding(0, 8, 0, 8);
        root.addView(text);
    }
}

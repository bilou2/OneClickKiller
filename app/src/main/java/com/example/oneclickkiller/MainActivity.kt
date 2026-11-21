package com.example.oneclickkiller

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val btnKillAll: Button = findViewById(R.id.btnKillAll)

        btnKillAll.setOnClickListener {
            if (!isAccessibilityServiceEnabled(this, AutoKillService::class.java)) {
                Toast.makeText(this, "Active le service 'OneClickKiller' dans l'accessibilité", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } else {
                startKillingProcess()
            }
        }
    }

    private fun startKillingProcess() {
        val pm = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        val appsToKill = packages.filter { appInfo ->
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && appInfo.packageName != packageName
        }.map { it.packageName }

        if (appsToKill.isEmpty()) {
            statusText.text = "Aucune application tierce à fermer."
            return
        }

        AutoKillService.isKilling = true
        statusText.text = "Fermeture de ${appsToKill.size} applications en cours..."

        val handler = Handler(Looper.getMainLooper())
        appsToKill.forEachIndexed { index, pkgName ->
            handler.postDelayed({
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$pkgName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)

                if (index == appsToKill.size - 1) {
                    handler.postDelayed({
                        AutoKillService.isKilling = false
                        val backHome = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        }
                        startActivity(backHome)
                        statusText.text = "Terminé ! Toutes les tâches ont été arrêtées."
                    }, 1000)
                }
            }, index * 900L)
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expectedComponentName = "${context.packageName}/${service.name}"
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            if (colonSplitter.next().equals(expectedComponentName, ignoreCase = true)) return true
        }
        return false
    }
}

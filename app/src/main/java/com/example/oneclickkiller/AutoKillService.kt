package com.example.oneclickkiller

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AutoKillService : AccessibilityService() {

    companion object {
        var isKilling = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isKilling) return
        val rootNode = rootInActiveWindow ?: return

        val forceStopTexts = listOf("Forcer l'arrêt", "Force stop", "Forcer l’arrêt")
        for (text in forceStopTexts) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(text)
            for (node in nodes) {
                if (node.isEnabled && node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                } else if (node.parent != null && node.parent.isEnabled && node.parent.isClickable) {
                    node.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }

        val confirmTexts = listOf("OK", "Forcer l'arrêt", "Force stop", "Forcer l’arrêt")
        for (text in confirmTexts) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(text)
            for (node in nodes) {
                if (node.viewIdResourceName == "android:id/button1" && node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
    }

    override fun onInterrupt() {
        isKilling = false
    }
}

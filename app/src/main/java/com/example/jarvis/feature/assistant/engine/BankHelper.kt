package com.example.jarvis.feature.assistant.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.jarvis.core.memory.UserProfile

object BankHelper {

    private val commonBankPackages = listOf(
        "com.nu.production", // Nubank
        "com.itau",          // Itaú
        "com.bradesco",      // Bradesco
        "br.com.intermedium",// Banco Inter
        "br.com.bb.android"  // Banco do Brasil
    )

    fun checkBalance(context: Context, profile: UserProfile): String {
        val formattedBalance = "R$ %,.2f".format(profile.accountBalance)

        // Try to locate installed bank app to assist master
        var installedBank: String? = null
        for (pkg in commonBankPackages) {
            try {
                context.packageManager.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
                installedBank = pkg
                break
            } catch (_: Exception) {}
        }

        if (installedBank != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(installedBank)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return "Seu saldo registrado no cofre é de $formattedBalance, ${profile.masterTitle}. Por segurança bancária do Android e criptografia de ponta a ponta, estou abrindo seu banco para validação biométrica direta."
            }
        }

        return "Seu saldo registrado no cofre Stark é de $formattedBalance, ${profile.masterTitle}. Devido às normas de segurança bancária do Android (FLAG_SECURE), senhas de contas não podem ser acessadas por apps de terceiros sem a sua biometria."
    }
}

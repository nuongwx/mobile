package com.example.datto.Credential

import android.util.Log
import java.io.FileInputStream
import java.io.InputStream

class CredentialService {
    private val CREDENTIAL_DIR: String = "/data/data/com.example.datto/Credential"
    fun get(): String {
        // Get the credential from Credential file
        try {
            val inputStream: InputStream = FileInputStream(CREDENTIAL_DIR)

            // Read the credential from the file
            val credential = inputStream.bufferedReader().use { it.readText() }
            Log.d("CREDENTIAL_SERVICE", "Credential: $credential")

            return credential

        } catch (t: Throwable) {
            // Handle exception
            Log.e("CREDENTIAL_SERVICE", "Error in getting credential", t)
        }

        return ""
    }
}
package com.example.datto.Credential

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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

    fun set(newCredential: String) {
        try {
            val credentialFile = File(CREDENTIAL_DIR)
            if (!credentialFile.exists()) {
                credentialFile.createNewFile()
            }
            FileOutputStream(credentialFile).use {
                it.write(newCredential.toByteArray())
                Log.d("CREDENTIAL_SERVICE", "Credential set successfully")
            }
        } catch (t: Throwable) {
            Log.e("CREDENTIAL_SERVICE", "Error in setting credential", t)
        }
    }

    fun erase() {
        try {
            val credentialFile = File(CREDENTIAL_DIR)
            if (credentialFile.exists()) {
                val fileOutputStream = FileOutputStream(credentialFile)
                fileOutputStream.channel.truncate(0)
                fileOutputStream.close()
                Log.d("CREDENTIAL_SERVICE", "Credential erased successfully")
            } else {
                Log.d("CREDENTIAL_SERVICE", "Credential file does not exist")
            }
        } catch (t: Throwable) {
            Log.e("CREDENTIAL_SERVICE", "Error in erasing credential content", t)
        }
    }
}
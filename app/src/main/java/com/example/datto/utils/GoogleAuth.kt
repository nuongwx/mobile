package com.example.datto.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.Credential.CredentialService
import com.example.datto.DataClass.GoogleAccountRequest
import com.example.datto.DataClass.NewAccountResponse
import com.example.datto.MainActivity
import com.example.datto.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class GoogleAuth(private val context: Context) {
    companion object {
        private const val TAG = "GoogleSignIn"
        private const val RC_SIGN_IN = 9001
    }

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleClient: GoogleSignInClient


    fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(context, gso)
        val signInIntent = googleClient.signInIntent
        (context as Activity).startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun onActivityResult(requestCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN && data != null) {
            val result = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = result.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(context, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(context as Activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val account = auth.currentUser
                    // Update UI or proceed with your app logic
                    if (account != null) {
                        Log.d(TAG, "firebaseAuthWithGoogle: ${account.displayName} ${account.email} ${account.photoUrl}")
                        sendRequest(account)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e(TAG, "Authentication failed", task.exception)
                    Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun sendRequest(account: FirebaseUser) {
        val newGoogleAccount = GoogleAccountRequest(account.email.toString(),
                                                    account.uid,
                                                    account.displayName.toString(),
                                                    account.photoUrl.toString())
        APIService(context).doPost<NewAccountResponse>(
            "accounts/auth-google",
            newGoogleAccount,
            object :
                APICallback<Any> {
                override fun onSuccess(data: Any) {
                    data as NewAccountResponse
                    Log.d("API_SERVICE", "Data: ${data.id}")
                    CredentialService().set(data.id)
                    googleClient.revokeAccess().addOnCompleteListener {
                        signOut()
                        val i = Intent(context, MainActivity::class.java)
                        context.startActivity(i)
                    }
                }

                override fun onError(error: Throwable) {
                    Log.e("API_SERVICE", "Error: ${error.message}")
                    Toast.makeText(
                        context,
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun isSignedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }
    fun signOut() {
        auth.signOut()
        googleClient.signOut()
    }
}

package com.example.android_launcher.utils

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider


class TwitterSignInUtils(private val activity: Activity) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val provider = OAuthProvider.newBuilder("x.com")

    init {
        // Configure the OAuth provider with additional scopes (optional)
        provider.scopes = listOf("tweet.read", "users.read", "follows.read")
        // Add custom parameters if needed
        provider.addCustomParameters(mapOf(
            "lang" to "en"
        ))
    }


    fun signInWithTwitter(onSuccess: (FirebaseUser) -> Unit, onError: (Exception) -> Unit) {
        // Check if there's a pending result first
//        val isXInstalled = isAppInstalled(activity,"com.twitter.android")
//        val isXLiteInstalled = isAppInstalled(activity,"com.twitter.android.lite")
//        if (!isXInstalled || !isXLiteInstalled){
//            onError(Exception("X(twitter) not installed, please install to proceed."))
//            return
//        }
        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                .addOnSuccessListener { authResult ->
                    authResult.user?.let { user ->
                        onSuccess(user)
                    } ?: onError(Exception("User is null"))
                }
                .addOnFailureListener { exception ->
                    onError(exception)
                }
        } else {
            // Start the sign-in flow
            auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener { authResult ->
                    // User is signed in
                    authResult.user?.let { user ->
                        handleSignInResult(user, onSuccess, onError)
                    } ?: onError(Exception("User is null after sign-in"))
                }
                .addOnFailureListener { exception ->
                    onError(exception)
                }
        }
    }

    /**
     * Handle the sign-in result and extract user info
     */
    private fun handleSignInResult(
        user: FirebaseUser,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            // Get additional Twitter user info from the credential
            val credential = user.providerData.find {
                it.providerId == "x.com"
            }

            credential?.let {
                Log.d("TwitterAuth", "Twitter Username: ${it.displayName}")
                Log.d("TwitterAuth", "Twitter UID: ${it.uid}")
                Log.d("TwitterAuth", "Profile Photo: ${it.photoUrl}")
            }

            onSuccess(user)
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Get current signed-in user
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Sign out the user
     */
    fun signOut(onComplete: () -> Unit) {
        auth.signOut()
        onComplete()
    }

    /**
     * Check if user is signed in
     */
    fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Get Twitter access token (if needed for Twitter API calls)
     */
    fun getTwitterAccessToken(callback: (String?) -> Unit) {
        auth.currentUser?.getIdToken(true)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Note: This gets Firebase token, not Twitter access token
                    // For Twitter API access token, you need to handle it differently
                    callback(task.result?.token)
                } else {
                    callback(null)
                }
            }
    }
}
//class TwitterSignInUtils {
//    companion object{
//        fun doTwitterSignIn(context: Context, login:()->Unit){
//            val auth = FirebaseAuth.getInstance()
//
//
//            val twitterProvider = TwitterAuthProvider.getCredential(
//                "4634554455-Of7oItiTdy1ez4dv2Dp8Qp4mOCGnaxsOAcAVw6x",
//                "owFrb3fsMxzca9DXYRsYd226CuMJNR7b3GkCIy7SanzGX")
//            auth.signInWithCredential(twitterProvider)
//                .addOnSuccessListener {
//                    Log.d("user_authenticated","user=${it.user?.displayName}")
//                    it.user
//                    Toast.makeText(context,"Sign in successful", Toast.LENGTH_SHORT).show()
//                    login()
//                }
//                .addOnFailureListener { it->
//                    Log.e("error_twitter","error=${it.message}")
//                    Toast.makeText(context,"Failed. ${it.message}", Toast.LENGTH_SHORT).show()
//                }
//
//        }
//    }
//    fun signinTwitter(context: Context, login:()->Unit,onError:(Exception)->Unit){
//        val provider = OAuthProvider.newBuilder("twitter.com")
//        val auth = FirebaseAuth.getInstance()
//        val pendingResultTask = auth.pendingAuthResult
//        if (pendingResultTask != null) {
//            // There's something already here! Finish the sign-in for your user.
//            pendingResultTask
//                .addOnSuccessListener { authResult ->
//                    authResult.user?.let { user ->
////                        onSuccess()
//                        login()
//                    } ?: onError(Exception("User is null"))
//                }
//                .addOnFailureListener { exception ->
//                    onError(exception)
//                }
//        } else {
//            // Start the sign-in flow
//            auth.startActivityForSignInWithProvider(this, provider.build())
//                .addOnSuccessListener { authResult ->
//                    // User is signed in
//                    authResult.user?.let { user ->
//                        handleSignInResult(user, onSuccess, onError)
//                    } ?: onError(Exception("User is null after sign-in"))
//                }
//                .addOnFailureListener { exception ->
//                    onError(exception)
//                }
//        }
////        auth.signInWithCredential(provider)
//    }
//}
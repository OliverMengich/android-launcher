package com.example.android_launcher.presentation.screens.onboarding

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.core.net.toUri
import androidx.credentials.provider.Action
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.example.android_launcher.LOGGED_IN_USER_NAME
import com.example.android_launcher.dataStore
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.actionCodeSettings
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OnboardingViewModel(private val context: Context): ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _finishNav = Channel<Boolean>()
    val finishNav = _finishNav.receiveAsFlow()
    fun requestUsageStatsPermission(){
        if (!hasUsageAccess(context)) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
    fun isDefaultLauncher(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)

        val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName == context.packageName
    }
    fun loginUser (isLoggedIn: Boolean){
        viewModelScope.launch {
            context.dataStore.updateData {
                it.copy(
                    isLoggedIn = isLoggedIn
                )
            }
        }
    }
    fun requestOverlayPermission(){
        if (!Settings.canDrawOverlays(context)){
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${context.packageName}".toUri()
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
    fun requestSetDefaultLauncher(){
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun loginWithEmailAndPassword(email: String, password: String){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(context,"Sign in successful.", Toast.LENGTH_SHORT).show()
                    viewModelScope.launch {
                        _finishNav.send(true)
                    }
                }else{
                    val exception = task.exception
                    when (exception) {
                        is FirebaseAuthWeakPasswordException ->{
                            Toast.makeText(context,"${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> {

                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { signUpTask ->
                                    if (signUpTask.isSuccessful) {
                                        val newUser = auth.currentUser
                                        Toast.makeText(context,"Account created", Toast.LENGTH_SHORT).show()
                                        Log.d("AUTH", "User created: ${newUser?.email}")
                                        viewModelScope.launch {
                                            _finishNav.send(true)
                                        }
                                    } else {
                                        Toast.makeText(context,"Account created. .", Toast.LENGTH_SHORT).show()

                                        Log.e("AUTH", "Sign-up failed: ${signUpTask.exception?.message}")
                                    }
                                }
                        }
                        else -> {
                            Toast.makeText(context,"Sign-in failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

}
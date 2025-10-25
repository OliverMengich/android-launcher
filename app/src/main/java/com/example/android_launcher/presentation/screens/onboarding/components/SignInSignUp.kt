package com.example.android_launcher.presentation.screens.onboarding.components

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import com.example.android_launcher.utils.GoogleSignInUtils
import com.example.android_launcher.IS_LOGGED_IN_KEY
import com.example.android_launcher.R
import com.example.android_launcher.dataStore
import com.example.android_launcher.utils.TwitterSignInUtils
import kotlinx.coroutines.launch

@Composable
fun SignInSignUp(navigateToNextPage:()-> Unit,navigateToPrev: ()->Unit,loginWithEmailAndPassword:(String,String)->Unit){
    val context = LocalContext.current
    val activity = context as Activity
    val twitterAuthManager = remember { TwitterSignInUtils(activity) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        GoogleSignInUtils.doGoogleSignIn(
            context = context,
            scope = scope,
            launcher = null,
            login = {
                Toast.makeText(context,"Login successful.", Toast.LENGTH_SHORT).show()
                navigateToNextPage()
                isLoading = false
            },
            onError = { e->
                Toast.makeText(context,"Login error ${e.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        )
    }
    fun launchTwitterSignIn(){
        isLoading = true
        twitterAuthManager.signInWithTwitter(
            onSuccess = { user->
                Toast.makeText(context,"Welcome ${user.displayName}. Auth successful", Toast.LENGTH_SHORT).show()
                isLoading = false
                navigateToNextPage()
            },
            onError = { e->
                isLoading = false
                Toast.makeText(context,"Error ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("twitter_Error",e.message.toString())
            }
        )
//        TwitterSignInUtils.doTwitterSignIn(
//            context = context,
//            login = {
//                Toast.makeText(context,"Login successful.", Toast.LENGTH_SHORT).show()
//                navigateToNextPage()
//            }
//        )
    }
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(.2f), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("1. Login/Register with us.", fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
            }
            Spacer(modifier = Modifier.height(36.dp))
            Column {
                TextInput(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    title = "Email",
                    placeHolder = "hello@gmail.com"
                )
                TextInput(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    title = "Password",
                    placeHolder = "*******"
                )
                SocialButton(
                    title = "Continue with Email",
                    onClick = {
                        if (password=="" || email==""){
                            Toast.makeText(context,"Please ensure all values are filled.", Toast.LENGTH_SHORT).show()
                        }else {
                            loginWithEmailAndPassword( email,password)
                        }
                    },
                    icon = R.drawable.ic_lock,
                )
            }
            Column(modifier = Modifier.fillMaxWidth(),horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth(fraction=0.9f).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp
                    )
                    Text("OR", modifier = Modifier.padding(horizontal = 10.dp))
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp
                    )
                }
                SocialButton(
                    title = "Continue with Google",
                    onClick = {
                        GoogleSignInUtils.doGoogleSignIn(
                            context = context,
                            scope = scope,
                            launcher = launcher,
                            login = { user->
                                Toast.makeText(context,"Welcome ${user.displayName}. Auth successful", Toast.LENGTH_SHORT).show()
                                isLoading = false
                                navigateToNextPage()
                                scope.launch {
                                    context.dataStore.edit { st ->
                                        st[IS_LOGGED_IN_KEY] = true
                                    }
                                }
                            },
                            onError = { e->
                                Toast.makeText(context,"Auth failed ${e.message}", Toast.LENGTH_SHORT).show()
                                isLoading = false
                            }
                        )
                    },
                    icon = R.drawable.google,
                )
//                SocialButton(
//                    title = "Continue with Twitter",
//                    onClick = {
//                        launchTwitterSignIn()
//                    },
//                    icon = R.drawable.x,
//                )
            }
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)){
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = navigateToPrev,
                    content = {
                        Text("PREVIOUS",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                )
                TextButton(onClick = navigateToNextPage,
                    content = {
                        Text("SKIP FOR NOW",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 15.sp,
                        )
                    }
                )
            }
        }
        if (isLoading){
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha=.5f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(150.dp))
            }
        }
    }
}

@Composable
fun SocialButton(title: String, onClick: ()-> Unit, icon: Int,){
    Button(
        modifier = Modifier.fillMaxWidth(.9f).padding(vertical = 10.dp).background(Color.Transparent).border(width = .5.dp, color = Color.LightGray, shape = RoundedCornerShape(5.dp)),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor =  Color.Transparent,
        ),
        shape = RoundedCornerShape(5.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 10.dp).size(20.dp)
        )
        Text(text=title,color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun TextInput(value: String, title: String, keyboardOptions: KeyboardOptions= KeyboardOptions(keyboardType = KeyboardType.Text), onValueChange: (String)-> Unit,placeHolder: String){
    Column(Modifier.padding(vertical = 10.dp)) {
        Text(title)
        OutlinedTextField(
            value = value,
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                focusedBorderColor = Color(0xFFFFAA6E),
                unfocusedContainerColor = Color.Transparent,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
            ),
            modifier = Modifier.fillMaxWidth(.9f),
            placeholder = {
                Text(placeHolder)
            },
            onValueChange = onValueChange
        )
    }
}
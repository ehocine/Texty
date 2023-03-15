package com.hocel.texty.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.hocel.texty.data.models.ScannedText
import com.hocel.texty.data.models.User
import com.hocel.texty.navigation.Screens
import com.hocel.texty.utils.Constants.FIRESTORE_USERS_DATABASE
import com.hocel.texty.utils.Constants.LIST_OF_SCANNED_TEXTS
import com.hocel.texty.utils.Constants.TIMEOUT_IN_MILLIS
import com.hocel.texty.utils.Constants.auth
import com.hocel.texty.utils.Constants.loadingState
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


//Register new user
fun registerNewUser(
    navController: NavController,
    context: Context,
    userName: String,
    emailAddress: String,
    password: String
) {
    if (hasInternetConnection(context)) {
        if (emailAddress.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        loadingState.emit(LoadingState.LOADING)
                        auth.createUserWithEmailAndPassword(emailAddress, password).await()
                        loadingState.emit(LoadingState.LOADED)
                        withContext(Dispatchers.Main) {
                            val user = Firebase.auth.currentUser
                            val setUserName = userProfileChangeRequest {
                                displayName = userName
                            }
                            user!!.updateProfile(setUserName).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("Tag", user.displayName.toString())
                                    createUser(user)
                                }
                            }
                            user.sendEmailVerification().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    "Verification email sent".toast(context, Toast.LENGTH_SHORT)
                                }
                            }
                            navController.navigate(Screens.Login.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    } ?: withContext(Dispatchers.Main) {
                        loadingState.emit(LoadingState.ERROR)
                        "Time out: couldn't connect".toast(context, Toast.LENGTH_SHORT)
                    }
                } catch (e: Exception) {
                    loadingState.emit(LoadingState.ERROR)
                    withContext(Dispatchers.Main) {
                        Log.d("Tag", "Register: ${e.message}")
                    }
                }
            }
        } else {
            "Please verify your inputs".toast(context, Toast.LENGTH_SHORT)
        }
    } else {
        "Device is not connected to the internet".toast(context, Toast.LENGTH_SHORT)
    }
}

//Sign in existing user
fun signInUser(
    navController: NavController,
    context: Context,
    emailAddress: String,
    password: String
) {
    if (hasInternetConnection(context)) {
        if (emailAddress.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        loadingState.emit(LoadingState.LOADING)
                        auth.signInWithEmailAndPassword(emailAddress, password).await()
                        loadingState.emit(LoadingState.LOADED)
                        val user = Firebase.auth.currentUser
                        if (user!!.isEmailVerified) {
                            withContext(Dispatchers.Main) {
                                navController.navigate(Screens.HomeScreen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                "Your email address is not verified yet".toast(
                                    context,
                                    Toast.LENGTH_SHORT
                                )
                            }
                        }
                    } ?: withContext(Dispatchers.Main) {
                        loadingState.emit(LoadingState.ERROR)
                        "Time out: couldn't connect".toast(context, Toast.LENGTH_SHORT)
                    }
                } catch (e: Exception) {
                    loadingState.emit(LoadingState.ERROR)
                    withContext(Dispatchers.Main) {
                        Log.d("Tag", "Sign in: ${e.message}")
//                        snackbar(
//                            "${e.message}",
//                            SnackbarDuration.Short
//                        )
                    }
                }
            }
        } else {
            "Please verify your inputs".toast(context, Toast.LENGTH_SHORT)
        }
    } else {
        "Device is not connected to the internet".toast(context, Toast.LENGTH_SHORT)
    }
}


// Function to create a new user by getting the ID from the auth system
fun createUser(user: FirebaseUser?) {
    val db = Firebase.firestore
    val newUser = user?.let {
        User(
            userID = it.uid,
            name = it.displayName.toString(),
            email = it.email.toString(),
            listOfScannedText = listOf()
        )
    }
    if (newUser != null) {
        db.collection(FIRESTORE_USERS_DATABASE).document(user.uid)
            .set(newUser)
            .addOnCompleteListener { task ->
                Log.d("Tag", "success $task")
            }.addOnFailureListener { task ->
                Log.d("Tag", "Failure $task")
            }
    }
}

//Reset password function
fun resetUserPassword(
    context: Context,
    emailAddress: String
) {
    if (hasInternetConnection(context)) {
        if (emailAddress.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        loadingState.emit(LoadingState.LOADING)
                        auth.sendPasswordResetEmail(emailAddress).await()
                        loadingState.emit(LoadingState.LOADED)
                        withContext(Dispatchers.Main) {
                            "Email sent".toast(context, Toast.LENGTH_SHORT)
                        }
                    } ?: withContext(Dispatchers.Main) {
                        loadingState.emit(LoadingState.ERROR)
                        "Time out: couldn't connect".toast(context, Toast.LENGTH_SHORT)
                    }
                } catch (e: Exception) {
                    loadingState.emit(LoadingState.ERROR)
                    withContext(Dispatchers.Main) {
                        Log.d("Tag", "Reset: ${e.message}")
//                        snackbar(
//                            e.message.toString(),
//                            SnackbarDuration.Short
//                        )
                    }
                }
            }
        } else {
            "Please verify your inputs".toast(context, Toast.LENGTH_SHORT)
        }
    } else {
        "Device is not connected to the internet".toast(context, Toast.LENGTH_SHORT)
    }
}


fun resendVerificationEmail(
    context: Context
) {
    val user = auth.currentUser
    if (user != null) {
        if (!user.isEmailVerified) {
            user.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    "Verification email sent".toast(context, Toast.LENGTH_SHORT)
                }
            }.addOnFailureListener {
            }
        } else {
            "Your email has already been verified".toast(context, Toast.LENGTH_SHORT)
        }
    } else {
        "An error occurred".toast(context, Toast.LENGTH_SHORT)
    }
}

fun uploadScannedTextImage(
    fileUri: Uri,
    scannedText: ScannedText
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Get current username
            val currentUser = Firebase.auth.currentUser

            // Create a storage reference from our app
            val storageRef = Firebase.storage.reference

            val profileRef =
//                currentUser?.let { storageRef.child("${it.uid}/scannedTexts/${fileUri.lastPathSegment}") }
                currentUser?.let { storageRef.child("${it.uid}/scannedTexts/${scannedText.scannedTime}") }

            // Upload file
            profileRef?.putFile(fileUri)?.addOnSuccessListener {

                currentUser.let { firebaseUser ->
                    storageRef.child("${firebaseUser.uid}/scannedTexts/${scannedText.scannedTime}")
                        .downloadUrl.addOnSuccessListener {
                            updateScannedTextImage(
                                scannedText = scannedText,
                                pictureUri = it.toString()
                            )
                        }.addOnFailureListener {
                        }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


fun updateScannedTextImage(
    pictureUri: String,
    scannedText: ScannedText
) {
    val db = Firebase.firestore
    val currentUser = Firebase.auth.currentUser
    CoroutineScope(Dispatchers.IO).launch {
        val dataOfUser =
            currentUser?.let { db.collection(FIRESTORE_USERS_DATABASE).document(it.uid) }
        dataOfUser?.update(
            LIST_OF_SCANNED_TEXTS,
            FieldValue.arrayRemove(scannedText)
        )?.addOnSuccessListener {
            scannedText.imageUri = pictureUri
            dataOfUser.update(
                LIST_OF_SCANNED_TEXTS,
                FieldValue.arrayUnion(scannedText)
            )
        }?.addOnFailureListener {
            Log.d("Error", it.toString())
        }
    }
}

fun deleteImageFromStorage(scannedText: ScannedText) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Get current username
            val currentUser = Firebase.auth.currentUser

            // Create a storage reference from our app
            val storageRef = Firebase.storage.reference

            val profileRef =
//                currentUser?.let { storageRef.child("${it.uid}/scannedTexts/${fileUri.lastPathSegment}") }
                currentUser?.let { storageRef.child("${it.uid}/scannedTexts/${scannedText.scannedTime}") }

            // Delete file
            profileRef?.delete()?.addOnSuccessListener {
            }?.addOnFailureListener {

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Function to check is the user is not null and has email verified
fun userLoggedIn(): Boolean {
    val user = Firebase.auth.currentUser
    return user != null && user.isEmailVerified
}
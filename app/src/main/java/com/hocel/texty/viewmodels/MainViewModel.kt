package com.hocel.texty.viewmodels

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hocel.texty.TextyApplication
import com.hocel.texty.data.models.ScannedText
import com.hocel.texty.data.models.User
import com.hocel.texty.utils.*
import com.hocel.texty.utils.Constants.FIRESTORE_USERS_DATABASE
import com.hocel.texty.utils.Constants.LIST_OF_SCANNED_TEXTS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: TextyApplication
) : ViewModel() {

    @OptIn(ExperimentalMaterialApi::class)
    private val _modalBottomSheetState: MutableState<ModalBottomSheetState> =
        mutableStateOf(ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden))

    @OptIn(ExperimentalMaterialApi::class)
    val modalBottomSheetState: State<ModalBottomSheetState> = _modalBottomSheetState

    private var _selectedScannedText: MutableState<ScannedText> = mutableStateOf(
        ScannedText()
    )
    val selectedScannedText: State<ScannedText> = _selectedScannedText

    private var _localImageUri: MutableState<Uri> = mutableStateOf(Uri.parse(""))
    val localImageUri: State<Uri> = _localImageUri

    private var _scanningStatus = MutableStateFlow(ScanningStatus.IDLE)
    val scanningStatus: StateFlow<ScanningStatus> = _scanningStatus

    private var _userInfo: MutableStateFlow<User> = MutableStateFlow(User())
    var userInfo = _userInfo.asStateFlow()

    private var imageRotation = 0

    private var _gettingData = MutableStateFlow(LoadingState.IDLE)
    var gettingData: StateFlow<LoadingState> = _gettingData

    fun processImage(gotText: (text: String) -> Unit) {
        viewModelScope.launch {
            try {
                _scanningStatus.emit(ScanningStatus.LOADING)
                val bitmap =
                    application.contentResolver.openInputStream(_localImageUri.value)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                val image =
                    InputImage.fromBitmap(bitmap!!, determineImageOrientation(_localImageUri.value))
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val resultText = visionText.text
                        if (resultText.isNotEmpty()) {
                            gotText(resultText)
                        }
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
                _scanningStatus.emit(ScanningStatus.LOADED)
            } catch (e: IOException) {
                _scanningStatus.emit(ScanningStatus.ERROR)
                e.printStackTrace()
            }
        }
    }

    fun getUserInfo(context: Context) {
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val data = currentUser?.let { db.collection(FIRESTORE_USERS_DATABASE).document(it.uid) }
        if (hasInternetConnection(context)) {
            if (currentUser != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        _gettingData.emit(LoadingState.LOADING)
                        data?.addSnapshotListener { value, error ->
                            if (error != null) {
                                return@addSnapshotListener
                            }
                            if (value != null && value.exists()) {
                                _userInfo.value =
                                    value.toObject(User::class.java) ?: User()
                            } else {
                                "An error occurred".toast(context, Toast.LENGTH_SHORT)
                            }
                        }
                        _gettingData.emit(LoadingState.LOADED)
                    } catch (e: Exception) {
                        _gettingData.emit(LoadingState.ERROR)
                        withContext(Dispatchers.Main) {
                            "An error occurred".toast(context, Toast.LENGTH_SHORT)
                        }
                    }
                }
            }
        } else {
            "Device is not connected to the internet".toast(context, Toast.LENGTH_SHORT)
        }
    }

    fun addOrRemoveScannedText(
        context: Context,
        action: AddOrRemoveAction,
        scannedText: ScannedText,
        onAddSuccess: () -> Unit,
        onRemoveSuccess: () -> Unit
    ) {
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val data = currentUser?.let { db.collection(FIRESTORE_USERS_DATABASE).document(it.uid) }
        if (hasInternetConnection(context)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    when (action) {
                        AddOrRemoveAction.ADD -> {
                            data?.update(
                                LIST_OF_SCANNED_TEXTS,
                                FieldValue.arrayUnion(scannedText)
                            )?.addOnSuccessListener {
                                onAddSuccess()

                            }?.addOnFailureListener {
                                "Something went wrong: $it".toast(context, Toast.LENGTH_SHORT)
                            }
                        }
                        AddOrRemoveAction.REMOVE -> {
                            data?.update(
                                LIST_OF_SCANNED_TEXTS,
                                FieldValue.arrayRemove(scannedText)
                            )?.addOnSuccessListener {
                                onRemoveSuccess()
                            }?.addOnFailureListener {
                                "Something went wrong: $it".toast(context, Toast.LENGTH_SHORT)
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        withContext(Dispatchers.Main) {
                            "An error occurred".toast(context, Toast.LENGTH_SHORT)
                        }
                    }
                }
            }
        } else {
            "Device is not connected to the internet".toast(context, Toast.LENGTH_SHORT)
        }
    }

    fun selectScannedText(scannedText: ScannedText) {
        _selectedScannedText.value = scannedText
    }

    fun setLocalImageUri(imageUri: Uri?) {
        if (imageUri != null) {
            _localImageUri.value = imageUri
        }
    }

    private fun determineImageOrientation(imageUri: Uri): Int {
        var inputStream: InputStream? = null
        try {
            inputStream = application.contentResolver.openInputStream(imageUri)
            val exif = inputStream?.run { ExifInterface(this) }
            return when (exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return 0
    }
}
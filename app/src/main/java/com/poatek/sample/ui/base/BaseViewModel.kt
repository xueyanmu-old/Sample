package com.poatek.sample.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.poatek.sample.utils.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel() {

    val isLoading = MutableLiveData(false)

    private val mErrorDialog = SingleLiveEvent<AlertDialogContent>()
    val errorDialog: LiveData<AlertDialogContent>
        get() = mErrorDialog

    private val mToast = SingleLiveEvent<String>()
    val toast: LiveData<String>
        get() = mToast

    fun runLoading(
        context: CoroutineContext,
        loadStateLiveData: MutableLiveData<Boolean> = isLoading,
        block: suspend CoroutineScope.() -> Unit
    ) {
        if (loadStateLiveData.value == true) return
        loadStateLiveData.value = true
        viewModelScope.launch(context) {
            block.invoke(this)
            loadStateLiveData.postValue(false)
        }
    }

    fun displayErrorDialog(title: String, message: String, retryCallback: (() -> Unit)? = null) {
        if (!mErrorDialog.hasObservers()) {
            throw IllegalStateException("The error dialog has no observer.")
        }
        mErrorDialog.postValue(AlertDialogContent(title, message, retryCallback))
    }

    fun displayToast(message: String) {
        if (!mToast.hasObservers()) {
            throw IllegalStateException("The toast has no observer.")
        }
        mToast.postValue(message)
    }

}

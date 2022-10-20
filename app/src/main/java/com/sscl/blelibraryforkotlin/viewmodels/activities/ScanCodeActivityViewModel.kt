package com.sscl.blelibraryforkotlin.viewmodels.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ScanCodeActivityViewModel : ViewModel() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * object 声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    object ScanCodeActivityViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ScanCodeActivityViewModel () as T
        }
    }
}
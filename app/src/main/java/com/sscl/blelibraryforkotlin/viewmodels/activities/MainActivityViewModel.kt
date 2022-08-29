package com.sscl.blelibraryforkotlin.viewmodels.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * 主界面的ViewModel
 */
class MainActivityViewModel : ViewModel() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * object 声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    object MainActivityViewModelFactory:ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel() as T
        }
    }
}
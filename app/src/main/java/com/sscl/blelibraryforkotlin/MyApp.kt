package com.sscl.blelibraryforkotlin

import android.app.Application
import com.sscl.baselibrary.files.FileUtil
import com.sscl.baselibrary.utils.CrashHandler
import com.sscl.baselibrary.utils.DebugUtil
import com.sscl.baselibrary.utils.LogCatHelper
import com.sscl.bluetoothlowenergylibrary.BleManager
import com.sscl.bluetoothlowenergylibrary.Logger

/**
 * 应用程序Application类
 */
class MyApp : Application() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {
        lateinit var instance: MyApp
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onCreate() {
        super.onCreate()
        instance = this
        DebugUtil.setDebugFlag(true)
        FileUtil.init(this)
        BleManager.initialize(this)
        Logger.enableLog(true)
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 公开方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 初始化异常捕获与日志记录
     */
    fun initCrashAndLogcat() {
        CrashHandler.getInstance().init(this)
        LogCatHelper.getInstance().init()
    }
}
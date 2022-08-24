package com.sscl.bluetoothlowenergylibrary

import android.util.Log

/**
 * 日志打印工具类
 */
object Logger {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 枚举声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 打印日志等级
     */
    enum class LogLevel {
        /**
         * INFO
         */
        INFO,

        /**
         * error
         */
        ERROR,

        /**
         * debug
         */
        DEBUG,

        /**
         * warn
         */
        WARN,

        /**
         * verbose
         */
        VERBOSE
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 可变属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 是否启用调试打印
     */
    private var logEnable = false

    /**
     * 调试输出打印等级
     */
    private var logLevel = LogLevel.WARN

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 方法声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 设置是否启用调试打印
     */
    fun enableLog(enable: Boolean) {
        logEnable = enable
    }

    /**
     * 设置日志调试等级
     */
    fun setLogLevel(logLevel: LogLevel) {
        this.logLevel = logLevel
    }

    /**
     * 日志输出
     */
    internal fun log(tag: String, msg: String) {
        if (!logEnable) {
            return
        }
        when (logLevel) {
            LogLevel.WARN -> {
                Log.w(tag, msg)
            }
            LogLevel.DEBUG -> {
                Log.d(tag, msg)
            }
            LogLevel.INFO -> {
                Log.i(tag, msg)
            }
            LogLevel.ERROR -> {
                Log.e(tag, msg)
            }
            LogLevel.VERBOSE -> {
                Log.v(tag, msg)
            }
        }
    }
}
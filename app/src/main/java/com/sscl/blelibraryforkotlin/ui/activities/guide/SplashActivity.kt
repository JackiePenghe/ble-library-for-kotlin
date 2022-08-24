package com.sscl.blelibraryforkotlin.ui.activities.guide

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AlertDialog
import com.sscl.baselibrary.activity.BaseSplashActivity
import com.sscl.baselibrary.utils.BaseManager
import com.sscl.baselibrary.utils.PermissionUtil
import com.sscl.blelibraryforkotlin.MyApp
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.ui.activities.MainActivity

/**
 * 启动界面
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseSplashActivity() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {
        /**
         * 权限
         */
        private val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }


        /**
         * 权限请求码
         */
        private const val PERMISSION_REQUEST_CODE = 1
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* * * * * * * * * * * * * * * * * * * 常量属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 权限请求回调
     */
    private val onPermissionRequestResult = object : PermissionUtil.OnPermissionRequestResult {
        /**
         * 权限请求失败
         *
         * @param failedPermissions 请求失败的权限
         */
        override fun permissionRequestFailed(failedPermissions: Array<String>) {
            if (!PermissionUtil.isAnyPermissionAlwaysDenied(
                    this@SplashActivity,
                    *failedPermissions
                )
            ) {
                checkPermissions()
            } else {
                showNoPermissionDialog()
            }
        }

        /**
         * 权限请求成功
         */
        override fun permissionRequestSucceed() {
            doNext()
        }
    }

    /* * * * * * * * * * * * * * * * * * * 可变属性 * * * * * * * * * * * * * * * * * * */

    /**
     * 是否需要检查权限
     */
    private var needCheckPermission: Boolean = false

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 在本界面第一次启动时执行的操作
     */
    override fun onCreate() {
        checkPermissions()
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        if (needCheckPermission) {
            needCheckPermission = false
            checkPermissions()
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 检查权限
     */
    private fun checkPermissions() {
        val hasPermissions = PermissionUtil.hasPermissions(this, *PERMISSIONS)
        if (hasPermissions) {
            doNext()
        } else {
            requestPermissions()
        }
    }

    /**
     * 请求权限
     */
    private fun requestPermissions() {
        PermissionUtil.setOnPermissionRequestResult(onPermissionRequestResult)
        PermissionUtil.requestPermission(this, PERMISSION_REQUEST_CODE, *PERMISSIONS)
    }

    /**
     * 跳转到下一个界面
     */
    private fun doNext() {
        MyApp.instance.initCrashAndLogcat()
        toMainActivity()
    }

    /**
     * 跳转到主界面
     */
    private fun toMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * 显示没有权限的对话框
     */
    private fun showNoPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.no_permission_dialog_title)
            .setMessage(
                getString(
                    R.string.no_permission_dialog_msg,
                    getString(R.string.sdcard_permission)
                )
            )
            .setCancelable(false)
            .setPositiveButton(R.string.go_settings) { _, _ ->
                PermissionUtil.toSettingActivity(this@SplashActivity)
                BaseManager.handler.postDelayed({
                    needCheckPermission = true
                }, 500)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                finish()
            }
            .show()
    }
}
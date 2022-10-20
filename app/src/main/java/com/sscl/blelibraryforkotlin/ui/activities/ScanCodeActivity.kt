package com.sscl.blelibraryforkotlin.ui.activities

import android.content.Intent
import androidx.activity.viewModels
import cn.bingoogolapple.qrcode.core.QRCodeView
import com.sscl.baselibrary.utils.ToastUtil
import com.sscl.blelibraryforkotlin.R
import com.sscl.blelibraryforkotlin.databinding.ActivityScanCodeBinding
import com.sscl.blelibraryforkotlin.ui.base.BaseDataBindingActivity
import com.sscl.blelibraryforkotlin.viewmodels.activities.ScanCodeActivityViewModel

/**
 * 二维码扫码界面
 */
class ScanCodeActivity : BaseDataBindingActivity<ActivityScanCodeBinding>() {
    /**
     * 设置布局
     */
    override fun setLayout(): Int {
        return R.layout.activity_scan_code
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 静态声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    companion object {
        const val SCAN_RESULT = "scan_result"
        const val SCAN_RESULT_CODE = 1
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 属性声明
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * ViewModel
     */
    private val scanCodeActivityViewModel by viewModels<ScanCodeActivityViewModel> {
        ScanCodeActivityViewModel.ScanCodeActivityViewModelFactory
    }

    /**
     * 扫码回调
     */
    private val zbarViewDelegate = object : QRCodeView.Delegate {
        /**
         * 处理扫描结果
         *
         * @param result 摄像头扫码时只要回调了该方法 result 就一定有值，不会为 null。解析本地图片或 Bitmap 时 result 可能为 null
         */
        override fun onScanQRCodeSuccess(result: String?) {
            //停止识别
            binding.zbarView.stopSpot()
            val intent = Intent()
            intent.putExtra(SCAN_RESULT, result)
            setResult(SCAN_RESULT_CODE, intent)
            finish()
        }

        /**
         * 摄像头环境亮度发生变化
         *
         * @param isDark 是否变暗
         */
        override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
            if (isDark) {
                ToastUtil.toastLong(this@ScanCodeActivity, R.string.scan_code_light_dark_hint)
            }
        }

        /**
         * 处理打开相机出错
         */
        override fun onScanQRCodeOpenCameraError() {
            ToastUtil.toastLong(this@ScanCodeActivity, R.string.camera_open_failed)
            finish()
        }

    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 实现方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 在设置布局之前需要进行的操作
     */
    override fun doBeforeSetLayout() {

    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    override fun doBeforeInitOthers() {
        setTitleText(R.string.device_scan_title)
        binding.viewModel = scanCodeActivityViewModel
    }

    /**
     * 初始化控件数据
     */
    override fun initViewData() {

    }

    /**
     * 初始化其他数据
     */
    override fun initOtherData() {

    }

    /**
     * 初始化事件
     */
    override fun initEvents() {
        binding.zbarView.setDelegate(zbarViewDelegate)
        binding.openFlashBtn.setOnClickListener {
            binding.zbarView.openFlashlight()
        }
        binding.closeFlashBtn.setOnClickListener {
            binding.zbarView.closeFlashlight()
        }
    }

    /**
     * 在最后进行的操作
     */
    override fun doAfterAll() {

    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onResume() {
        super.onResume()
        // 显示扫描框，并开始识别
        binding.zbarView.startSpotAndShowRect()
    }

    override fun onPause() {
        super.onPause()
        binding.zbarView.closeFlashlight()
        // 关闭摄像头预览，并且隐藏扫描框
        binding.zbarView.stopCamera()
    }

    override fun onDestroy() {
        binding.zbarView.onDestroy()
        super.onDestroy()
    }
}
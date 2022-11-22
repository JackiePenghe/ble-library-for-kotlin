# BleLibraryForKotlin

#### 介绍
BLE蓝牙库-Kotlin版本

#### 软件架构
软件架构说明
基于Kotlin封装的android 蓝牙BLE库

#### 安装教程

1.  声明jitpack仓库
```gradle
 repositories {
     
     //jitpack仓库
     maven {
         url "https://jitpack.io"
     }
 }
```
2.  引入蓝牙BLE库
```gradle
implementation 'com.gitee.sscl:ble-library-for-kotlin:Tag'
```
Tag为版本号，当前最新版本号为
[![](https://jitpack.io/v/com.gitee.sscl/ble-library-for-kotlin.svg)](https://jitpack.io/#com.gitee.sscl/ble-library-for-kotlin)

3.  开始使用

#### 使用说明

1.  在Application中初始化
```kotlin
/**
 * 应用程序Application类
 */
class MyApp : Application() {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 重写方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    override fun onCreate() {
        super.onCreate()
        //初始化BLE库
        BleManager.initialize(this)
        //开启BLE库日志打印
        Logger.enableLog(true)
    }
}
```
2.  设备搜索

```kolin
/**
 * 初始化蓝牙扫描器
 */
private fun initBleScanner() {
    //创建一个新的BLE扫描器(不建议使用，除非你需要在多个界面同时保留扫描器并且需要同时进行设备的搜索)
    //bleScanner = BleManager.newBleScanner()
    //获取BLE扫描器单例，全局唯一，推荐使用此方式
    //bleScanner = BleManager.getBleScannerInstance()
    //设置相关回调
    bleScanner.setOnBleScanStateChangedListener(onBleScanListener)
}

//释放创建的BLE扫描器，如果你使用的扫描器使用BleManager.newBleScanner()创建，则用此方法进行释放
//BleManager.releaseBleScanner(bleScanner)
//释放BLE扫描器单例
BleManager.releaseBleScannerInstance()
```

3.  单设备连接

```kotlin
/**
 * 初始化BLE连接器
 */
private fun initBleConnector() {
    //获取单设备连接器单例
    bleConnectorInstance = BleManager.getBleConnectorInstance()
    //设置BLE连接相关回调
    bleConnectorInstance.setOnBleConnectStateChangedListener(onBleConnectStateChangedListener)
    //设置特征数据读取回调
    bleConnectorInstance.setOnCharacteristicReadDataListener(onCharacteristicReadDataListener)
    //设置特征数据写入回调（写入数据成功时才有回调）
    bleConnectorInstance.setOnCharacteristicWriteDataListener(onCharacteristicWriteDataListener)
    //设置设备通知回调
    bleConnectorInstance.setOnCharacteristicNotifyDataListener(
        onCharacteristicNotifyDataListener
    )
    //设置描述数据读取回调
    bleConnectorInstance.setOnDescriptorReadDataListener(onDescriptorReadDataListener)
    //设置描述数据写入回调（写入数据成功时才有回调）
    bleConnectorInstance.setOnDescriptorWriteDataListener(onDescriptorWriteDataListener)
    //设置可靠数据写入完成的回调（需要设备端也支持可靠数据相关的流程）
    bleConnectorInstance.setOnReliableWriteCompletedListener(onReliableWriteCompletedListener)
    //设置读取设备信号强度回调
    bleConnectorInstance.setOnReadRemoteRssiListener(onReadRemoteRssiListener)
    //设置MTU(数据传输大小)变更的回调
    bleConnectorInstance.setOnMtuChangedListener(onMtuChangedListener)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //设置物理层读取的回调
        bleConnectorInstance.setOnPhyReadListener(onPhyReadListener)
        //设置物理层变更的回调
        bleConnectorInstance.setOnPhyUpdateListener(onPhyUpdateListener)
    }
}
```

常用基本方法

```kotlin
/**
 * 请求连接设备
 *
 * @param address       设备地址
 * @param autoReconnect 是否自动重连
 * @param bleConnectTransport     GATT 连接到远程双模设备的首选传输方式
 * @param phyMask       用于连接到远程设备的首选物理层
 * @return true表示请求已成功发起
 */
@Synchronized
@JvmOverloads
fun connect(
    address: String,
    autoReconnect: Boolean = false,
    bleConnectTransport: BleConnectTransport? = null,
    phyMask: BleConnectPhyMask? = null
): Boolean

/**
 * 请求连接设备
 *
 * @param bluetoothDevice 蓝牙设备
 * @param autoReconnect   是否自动重连
 * @param bleConnectTransport       GATT 连接到远程双模设备的首选传输方式
 * @param phyMask         用于连接到远程设备的首选物理层
 * @return true表示请求已成功发起，真正的连接结果在回调中
 */
@Synchronized
@JvmOverloads
fun connect(
    bluetoothDevice: BluetoothDevice,
    autoReconnect: Boolean = false,
    bleConnectTransport: BleConnectTransport? = null,
    phyMask: BleConnectPhyMask? = null
): Boolean

/**
 * 获取服务列表
 * @return 服务列表
 */
fun getServices(): MutableList<BluetoothGattService>?

/**
 *判断某个特征是否有对应的属性
 * @param serviceUuidString 服务UUID字符串
 * @param characteristicUuidString 特征UUID字符串
 * @return 是否有对应的属性
 */
fun checkCharacteristicProperties(
    serviceUuidString: String,
    characteristicUuidString: String,
    properties: Int
): Boolean

/**
 * 读取特征数据
 * @param serviceUuidString 服务UUID字符串
 * @param characteristicUuidString 特征UUID字符串
 * @return  是否执行成功
 */
fun readCharacteristicData(
    serviceUuidString: String,
    characteristicUuidString: String
): Boolean

/**
 * 写入特征数据
 * @param serviceUuidString 服务UUID字符串
 * @param characteristicUuidString 特征UUID字符串
 * @param byteArray 数据内容
 * @return 是否执行成功
 */
fun writeCharacteristicData(
    serviceUuidString: String,
    characteristicUuidString: String,
    byteArray: ByteArray
): Boolean

/**
 * 打开通知
 * @param serviceUuidString 服务UUID字符串
 * @param characteristicUuidString 特征UUID字符串
 * @param enable 是否开启通知
 * @return 是否执行成功
 */
fun enableNotification(
    serviceUuidString: String,
    characteristicUuidString: String,
    enable: Boolean
): Boolean
```

更多方法可下载源码阅读，或引入仓库后直接使用，本仓库理论支持全部BLE API （android api 21 ~ 32）

4. 多设备同时连接

使用BleMultiConnector代替BleSingleConnector即可

```kotlin

//因为需要区分多个设备，所以需要传入address以作区分

/**
 * 初始化BLE连接器
 */
private fun initBleConnector() {
    bleMultiConnector = BleManager.getBleMultiConnector()
    bleMultiConnector.setOnCharacteristicReadDataListener(
        address,
        onCharacteristicReadDataListener
    )
    bleMultiConnector.setOnCharacteristicWriteDataListener(
        address,
        onCharacteristicWriteDataListener
    )
    bleMultiConnector.setOnCharacteristicNotifyDataListener(
        address, onCharacteristicNotifyDataListener
    )
    bleMultiConnector.setOnDescriptorReadDataListener(address, onDescriptorReadDataListener)
    bleMultiConnector.setOnDescriptorWriteDataListener(address, onDescriptorWriteDataListener)
    bleMultiConnector.setOnReliableWriteCompletedListener(
        address,
        onReliableWriteCompletedListener
    )
    bleMultiConnector.setOnReadRemoteRssiListener(address, onReadRemoteRssiListener)
    bleMultiConnector.setOnMtuChangedListener(address, onMtuChangedListener)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        bleMultiConnector.setOnPhyReadListener(address, onPhyReadListener)
        bleMultiConnector.setOnPhyUpdateListener(address, onPhyUpdateListener)
    }
}
```

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)

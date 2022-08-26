package com.sscl.bluetoothlowenergylibrary.utils

import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import com.sscl.bluetoothlowenergylibrary.AdvertiseStruct
import com.sscl.bluetoothlowenergylibrary.ServiceDataInfo
import java.util.*
import kotlin.collections.ArrayList

/**
 * UUID名称获取工具类
 *
 * @author jackie
 */
object BleUtils {

    private val TAG: String = BleUtils::class.java.simpleName

    private val ATTRIBUTES: MutableMap<String, String> = HashMap()
    private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

    init {
        ATTRIBUTES["00001800-0000-1000-8000-00805f9b34fb"] = "GenericAccess"
        ATTRIBUTES["00001801-0000-1000-8000-00805f9b34fb"] = "GenericAttribute"
        ATTRIBUTES["00002800-0000-1000-8000-00805f9b34fb"] = "Primary Service"
        ATTRIBUTES["00002801-0000-1000-8000-00805f9b34fb"] = "Secondary Service"
        ATTRIBUTES["00002802-0000-1000-8000-00805f9b34fb"] = "Include"
        ATTRIBUTES["00002803-0000-1000-8000-00805f9b34fb"] = "Characteristic"
        ATTRIBUTES["00002900-0000-1000-8000-00805f9b34fb"] = "Characteristic Extended Properties"
        ATTRIBUTES["00002901-0000-1000-8000-00805f9b34fb"] = "Characteristic User Description"
        ATTRIBUTES["00002902-0000-1000-8000-00805f9b34fb"] = "Client Characteristic Configuration"
        ATTRIBUTES["00002903-0000-1000-8000-00805f9b34fb"] = "Server Characteristic Configuration"
        ATTRIBUTES["00002904-0000-1000-8000-00805f9b34fb"] =
            "Characteristic Presentation Format"
        ATTRIBUTES["00002905-0000-1000-8000-00805f9b34fb"] = "Characteristic Aggregate Format"
        ATTRIBUTES["00002906-0000-1000-8000-00805f9b34fb"] = "Valid Range"
        ATTRIBUTES["00002907-0000-1000-8000-00805f9b34fb"] = "External Report Reference Descriptor"
        ATTRIBUTES["00002908-0000-1000-8000-00805f9b34fb"] = "Report Reference Descriptor"
        ATTRIBUTES["00002a00-0000-1000-8000-00805f9b34fb"] = "Device Name"
        ATTRIBUTES["00002a01-0000-1000-8000-00805f9b34fb"] = "Appearance"
        ATTRIBUTES["00002a02-0000-1000-8000-00805f9b34fb"] = "Peripheral Privacy Flag"
        ATTRIBUTES["00002a03-0000-1000-8000-00805f9b34fb"] = "Reconnection Address"
        ATTRIBUTES["00002a04-0000-1000-8000-00805f9b34fb"] = "PPCP"
        ATTRIBUTES["00002a05-0000-1000-8000-00805f9b34fb"] = "Service Changed"
        ATTRIBUTES["00001802-0000-1000-8000-00805f9b34fb"] = "Immediate Alert"
        ATTRIBUTES["00001803-0000-1000-8000-00805f9b34fb"] = "Link Loss"
        ATTRIBUTES["00001804-0000-1000-8000-00805f9b34fb"] = "Tx Power"
        ATTRIBUTES["00001805-0000-1000-8000-00805f9b34fb"] = "Current Time Service"
        ATTRIBUTES["00001806-0000-1000-8000-00805f9b34fb"] = "Reference Time Update Service"
        ATTRIBUTES["00001807-0000-1000-8000-00805f9b34fb"] = "Next DST Change Service"
        ATTRIBUTES["00001808-0000-1000-8000-00805f9b34fb"] = "Glucose"
        ATTRIBUTES["00001809-0000-1000-8000-00805f9b34fb"] = "Health Thermometer"
        ATTRIBUTES["0000180a-0000-1000-8000-00805f9b34fb"] = "Device Information"
        ATTRIBUTES["0000180b-0000-1000-8000-00805f9b34fb"] = "Network Availability"
        ATTRIBUTES["0000180d-0000-1000-8000-00805f9b34fb"] = "Heart Rate"
        ATTRIBUTES["0000180e-0000-1000-8000-00805f9b34fb"] = "Phone Alert Status Service"
        ATTRIBUTES["0000180f-0000-1000-8000-00805f9b34fb"] = "Battery Service"
        ATTRIBUTES["00001810-0000-1000-8000-00805f9b34fb"] = "Blood Pressure"
        ATTRIBUTES["00001811-0000-1000-8000-00805f9b34fb"] = "Alert Notification Service"
        ATTRIBUTES["00001812-0000-1000-8000-00805f9b34fb"] = "Human Interface Device"
        ATTRIBUTES["00001813-0000-1000-8000-00805f9b34fb"] = "Scan Parameters"
        ATTRIBUTES["00001814-0000-1000-8000-00805f9b34fb"] = "Running Speed and Cadence"
        ATTRIBUTES["00001816-0000-1000-8000-00805f9b34fb"] = "Cycling Speed and Cadence"
        ATTRIBUTES["00001818-0000-1000-8000-00805f9b34fb"] = "Cycling Power"
        ATTRIBUTES["00001819-0000-1000-8000-00805f9b34fb"] = "Location and Navigation"
        ATTRIBUTES["00002700-0000-1000-8000-00805f9b34fb"] = "GATT_UNITLESS"
        ATTRIBUTES["00002701-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_LENGTH_METER"
        ATTRIBUTES["00002702-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_MASS_KGRAM"
        ATTRIBUTES["00002703-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_TIME_SECOND"
        ATTRIBUTES["00002704-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_ELECTRIC_CURRENT_A"
        ATTRIBUTES["00002705-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_THERMODYNAMIC_TEMP_K"
        ATTRIBUTES["00002706-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_AMOUNT_SUBSTANCE_M"
        ATTRIBUTES["00002707-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_LUMINOUS_INTENSITY_C"
        ATTRIBUTES["00002710-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_AREA_SQ_MTR"
        ATTRIBUTES["00002711-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_VOLUME_CUBIC_MTR"
        ATTRIBUTES["00002712-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_VELOCITY_MPS"
        ATTRIBUTES["00002713-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_ACCELERATION_MPS_SQ"
        ATTRIBUTES["00002714-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_WAVENUMBER_RM"
        ATTRIBUTES["00002715-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_DENSITY_KGPCM"
        ATTRIBUTES["00002716-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_SURFACE_DENSITY_KGPSM"
        ATTRIBUTES["00002717-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_SPECIFIC_VOLUME_CMPKG"
        ATTRIBUTES["00002718-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_CURRENT_DENSITY_APSM"
        ATTRIBUTES["00002719-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_MAGNETIC_FIELD_STRENGTH"
        ATTRIBUTES["0000271a-0000-1000-8000-00805f9b34fb"] =
            "GATT_UNIT_AMOUNT_CONCENTRATE_MPCM"
        ATTRIBUTES["0000271b-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_MASS_CONCENTRATE_KGPCM"
        ATTRIBUTES["0000271e-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_RELATIVE_PERMEABLILTY"
        ATTRIBUTES["00002720-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_PLANE_ANGLE_RAD"
        ATTRIBUTES["00002721-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_SOLID_ANGLE_STERAD"
        ATTRIBUTES["00002722-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_FREQUENCY_HTZ"
        ATTRIBUTES["00002723-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_FORCE_NEWTON"
        ATTRIBUTES["00002724-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_PRESSURE_PASCAL"
        ATTRIBUTES["00002725-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_ENERGY_JOULE"
        ATTRIBUTES["00002726-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_POWER_WATT"
        ATTRIBUTES["00002727-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_ELECTRIC_CHARGE_C"
        ATTRIBUTES["00002728-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_ELECTRIC_POTENTIAL_DIF_V"
        ATTRIBUTES["0000272f-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_CELSIUS_TEMP_DC"
        ATTRIBUTES["00002760-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_TIME_MINUTE"
        ATTRIBUTES["00002761-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_TIME_HOUR"
        ATTRIBUTES["00002762-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_TIME_DAY"
        ATTRIBUTES["00002763-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_PLANE_ANGLE_DEGREE"
        ATTRIBUTES["00002764-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_PLANE_ANGLE_MINUTE"
        ATTRIBUTES["00002765-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_PLANE_ANGLE_SECOND"
        ATTRIBUTES["00002766-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_AREA_HECTARE"
        ATTRIBUTES["00002767-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_VOLUME_LITRE"
        ATTRIBUTES["00002768-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_MASS_TONNE"
        ATTRIBUTES["000027a0-0000-1000-8000-00805f9b34fb"] = "GATT_UINT_LENGTH_YARD"
        ATTRIBUTES["000027a1-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_LENGTH_PARSEC"
        ATTRIBUTES["000027a2-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_LENGTH_INCH"
        ATTRIBUTES["000027a3-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_LENGTH_FOOT"
        ATTRIBUTES["000027a4-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_LENGTH_MILE"
        ATTRIBUTES["000027a5-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_PRESSURE_PFPSI"
        ATTRIBUTES["000027a6-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_VELOCITY_KMPH"
        ATTRIBUTES["000027a7-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_VELOCITY_MPH"
        ATTRIBUTES["000027a8-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_ANGULAR_VELOCITY_RPM"
        ATTRIBUTES["000027a9-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_ENERGY_GCAL"
        ATTRIBUTES["000027aa-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_ENERGY_KCAL"
        ATTRIBUTES["000027ab-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_ENERGY_KWH"
        ATTRIBUTES["000027ac-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_THERMODYNAMIC_TEMP_DF"
        ATTRIBUTES["000027ad-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_PERCENTAGE"
        ATTRIBUTES["000027ae-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_PER_MILE"
        ATTRIBUTES["000027af-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_PERIOD_BPM"
        ATTRIBUTES["000027b0-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_ELECTRIC_CHARGE_AH"
        ATTRIBUTES["000027b1-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_MASS_DENSITY_MGPD"
        ATTRIBUTES["000027b2-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_MASS_DENSITY_MMPL"
        ATTRIBUTES["000027b3-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_TIME_YEAR"
        ATTRIBUTES["000027b4-0000-1000-8000-00805f9b34fb"] = "GATT_UNIT_TIME_MONTH"
        ATTRIBUTES["00002a06-0000-1000-8000-00805f9b34fb"] = "Alert Level"
        ATTRIBUTES["00002a07-0000-1000-8000-00805f9b34fb"] = "Tx Power Level"
        ATTRIBUTES["00002a08-0000-1000-8000-00805f9b34fb"] = "Date Time"
        ATTRIBUTES["00002a09-0000-1000-8000-00805f9b34fb"] = "Day of Week"
        ATTRIBUTES["00002a0a-0000-1000-8000-00805f9b34fb"] = "Day Date Time"
        ATTRIBUTES["00002a0c-0000-1000-8000-00805f9b34fb"] = "Exact Time 256"
        ATTRIBUTES["00002a0d-0000-1000-8000-00805f9b34fb"] = "DST Offset"
        ATTRIBUTES["00002a0e-0000-1000-8000-00805f9b34fb"] = "Time Zone"
        ATTRIBUTES["00002a0f-0000-1000-8000-00805f9b34fb"] = "Local Time Information"
        ATTRIBUTES["00002a11-0000-1000-8000-00805f9b34fb"] = "Time with DST"
        ATTRIBUTES["00002a12-0000-1000-8000-00805f9b34fb"] = "Time Accuracy"
        ATTRIBUTES["00002a13-0000-1000-8000-00805f9b34fb"] = "Time Source"
        ATTRIBUTES["00002a14-0000-1000-8000-00805f9b34fb"] = "Reference Time Information"
        ATTRIBUTES["00002a16-0000-1000-8000-00805f9b34fb"] = "Time Update Control Point"
        ATTRIBUTES["00002a17-0000-1000-8000-00805f9b34fb"] = "Time Update State"
        ATTRIBUTES["00002a18-0000-1000-8000-00805f9b34fb"] = "Glucose Measurement"
        ATTRIBUTES["00002a19-0000-1000-8000-00805f9b34fb"] = "Battery Level"
        ATTRIBUTES["00002a1c-0000-1000-8000-00805f9b34fb"] = "Temperature Measurement"
        ATTRIBUTES["00002a1d-0000-1000-8000-00805f9b34fb"] = "Temperature Type"
        ATTRIBUTES["00002a1e-0000-1000-8000-00805f9b34fb"] = "Intermediate Temperature"
        ATTRIBUTES["00002a21-0000-1000-8000-00805f9b34fb"] = "Measurement Interval"
        ATTRIBUTES["00002a22-0000-1000-8000-00805f9b34fb"] = "Boot Keyboard Input Report"
        ATTRIBUTES["00002a23-0000-1000-8000-00805f9b34fb"] = "System ID"
        ATTRIBUTES["00002a24-0000-1000-8000-00805f9b34fb"] = "Model Number String"
        ATTRIBUTES["00002a25-0000-1000-8000-00805f9b34fb"] = "Serial Number String"
        ATTRIBUTES["00002a26-0000-1000-8000-00805f9b34fb"] = "Firmware Revision String"
        ATTRIBUTES["00002a27-0000-1000-8000-00805f9b34fb"] = "Hardware Revision String"
        ATTRIBUTES["00002a28-0000-1000-8000-00805f9b34fb"] = "Software Revision String"
        ATTRIBUTES["00002a29-0000-1000-8000-00805f9b34fb"] = "Manufacturer Name String"
        ATTRIBUTES["00002a2a-0000-1000-8000-00805f9b34fb"] =
            "IEEE 11073-20601 Regulatory Certification Data List"
        ATTRIBUTES["00002a2b-0000-1000-8000-00805f9b34fb"] = "Current Time"
        ATTRIBUTES["00002a31-0000-1000-8000-00805f9b34fb"] = "Scan Refresh"
        ATTRIBUTES["00002a32-0000-1000-8000-00805f9b34fb"] = "Boot Keyboard Output Report"
        ATTRIBUTES["00002a33-0000-1000-8000-00805f9b34fb"] = "Boot Mouse Input Report"
        ATTRIBUTES["00002a34-0000-1000-8000-00805f9b34fb"] = "Glucose Measurement Context"
        ATTRIBUTES["00002a35-0000-1000-8000-00805f9b34fb"] = "Blood Pressure Measurement"
        ATTRIBUTES["00002a36-0000-1000-8000-00805f9b34fb"] = "Intermediate Cuff Pressure"
        ATTRIBUTES["00002a37-0000-1000-8000-00805f9b34fb"] = "Heart Rate Measurement"
        ATTRIBUTES["00002a38-0000-1000-8000-00805f9b34fb"] = "Body Sensor Location"
        ATTRIBUTES["00002a39-0000-1000-8000-00805f9b34fb"] = "Heart Rate Control Point"
        ATTRIBUTES["00002a3e-0000-1000-8000-00805f9b34fb"] = "Network Availability"
        ATTRIBUTES["00002a3f-0000-1000-8000-00805f9b34fb"] = "Alert Status"
        ATTRIBUTES["00002a40-0000-1000-8000-00805f9b34fb"] = "Ringer Control Point"
        ATTRIBUTES["00002a41-0000-1000-8000-00805f9b34fb"] = "Ringer Setting"
        ATTRIBUTES["00002a42-0000-1000-8000-00805f9b34fb"] = "Alert Category ID Bit Mask"
        ATTRIBUTES["00002a43-0000-1000-8000-00805f9b34fb"] = "Alert Category ID"
        ATTRIBUTES["00002a44-0000-1000-8000-00805f9b34fb"] = "Alert Notification Control Point"
        ATTRIBUTES["00002a45-0000-1000-8000-00805f9b34fb"] = "Unread Alert Status"
        ATTRIBUTES["00002a46-0000-1000-8000-00805f9b34fb"] = "New Alert"
        ATTRIBUTES["00002a47-0000-1000-8000-00805f9b34fb"] = "Supported New Alert Category"
        ATTRIBUTES["00002a48-0000-1000-8000-00805f9b34fb"] = "Supported Unread Alert Category"
        ATTRIBUTES["00002a49-0000-1000-8000-00805f9b34fb"] = "Blood Pressure Feature"
        ATTRIBUTES["00002a4a-0000-1000-8000-00805f9b34fb"] = "HID Information"
        ATTRIBUTES["00002a4b-0000-1000-8000-00805f9b34fb"] = "Report Map"
        ATTRIBUTES["00002a4c-0000-1000-8000-00805f9b34fb"] = "HID Control Point"
        ATTRIBUTES["00002a4d-0000-1000-8000-00805f9b34fb"] = "Report"
        ATTRIBUTES["00002a4e-0000-1000-8000-00805f9b34fb"] = "Protocol Mode"
        ATTRIBUTES["00002a4f-0000-1000-8000-00805f9b34fb"] = "Scan Interval Window"
        ATTRIBUTES["00002a50-0000-1000-8000-00805f9b34fb"] = "PnP ID"
        ATTRIBUTES["00002a51-0000-1000-8000-00805f9b34fb"] = "Glucose Feature"
        ATTRIBUTES["00002a52-0000-1000-8000-00805f9b34fb"] = "Record Access Control Point"
        ATTRIBUTES["00002a53-0000-1000-8000-00805f9b34fb"] = "RSC Measurement"
        ATTRIBUTES["00002a54-0000-1000-8000-00805f9b34fb"] = "RSC Feature"
        ATTRIBUTES["00002a55-0000-1000-8000-00805f9b34fb"] = "SC Control Point"
        ATTRIBUTES["00002a5b-0000-1000-8000-00805f9b34fb"] = "CSC Measurement"
        ATTRIBUTES["00002a5c-0000-1000-8000-00805f9b34fb"] = "CSC Feature"
        ATTRIBUTES["00002a5d-0000-1000-8000-00805f9b34fb"] = "Sensor Location"
        ATTRIBUTES["00002a63-0000-1000-8000-00805f9b34fb"] = "Cycling Power Measurement"
        ATTRIBUTES["00002a64-0000-1000-8000-00805f9b34fb"] = "Cycling Power Vector"
        ATTRIBUTES["00002a65-0000-1000-8000-00805f9b34fb"] = "Cycling Power Feature"
        ATTRIBUTES["00002a66-0000-1000-8000-00805f9b34fb"] = "Cycling Power Control Point"
        ATTRIBUTES["00002a67-0000-1000-8000-00805f9b34fb"] = "Location and Speed"
        ATTRIBUTES["00002a68-0000-1000-8000-00805f9b34fb"] = "Navigation"
        ATTRIBUTES["00002a69-0000-1000-8000-00805f9b34fb"] = "Position Quality"
        ATTRIBUTES["00002a6a-0000-1000-8000-00805f9b34fb"] = "LN Feature"
        ATTRIBUTES["00002a6b-0000-1000-8000-00805f9b34fb"] = "LN Control Point"
        ATTRIBUTES["0000aa00-0000-1000-8000-00805f9b34fb"] = "IRTEMPERATURE_SERV"
        ATTRIBUTES["0000aa01-0000-1000-8000-00805f9b34fb"] = "IRTEMPERATURE_DATA"
        ATTRIBUTES["0000aa02-0000-1000-8000-00805f9b34fb"] = "IRTEMPERATURE_CONF"
        ATTRIBUTES["0000aa10-0000-1000-8000-00805f9b34fb"] = "ACCELEROMETER_SERV"
        ATTRIBUTES["0000aa11-0000-1000-8000-00805f9b34fb"] = "ACCELEROMETER_DATA"
        ATTRIBUTES["0000aa12-0000-1000-8000-00805f9b34fb"] = "ACCELEROMETER_CONF"
        ATTRIBUTES["0000aa13-0000-1000-8000-00805f9b34fb"] = "ACCELEROMETER_PERI"
        ATTRIBUTES["0000aa30-0000-1000-8000-00805f9b34fb"] = "MAGNETOMETER_SERV"
        ATTRIBUTES["0000aa31-0000-1000-8000-00805f9b34fb"] = "MAGNETOMETER_DATA"
        ATTRIBUTES["0000aa32-0000-1000-8000-00805f9b34fb"] = "MAGNETOMETER_CONF"
        ATTRIBUTES["0000aa33-0000-1000-8000-00805f9b34fb"] = "MAGNETOMETER_PERI"
        ATTRIBUTES["0000aa40-0000-1000-8000-00805f9b34fb"] = "BAROMETER_SERV"
        ATTRIBUTES["0000aa41-0000-1000-8000-00805f9b34fb"] = "BAROMETER_DATA"
        ATTRIBUTES["0000aa42-0000-1000-8000-00805f9b34fb"] = "BAROMETER_CONF"
        ATTRIBUTES["0000aa43-0000-1000-8000-00805f9b34fb"] = "BAROMETER_CALI"
        ATTRIBUTES["0000aa50-0000-1000-8000-00805f9b34fb"] = "GYROSCOPE_SERV"
        ATTRIBUTES["0000aa51-0000-1000-8000-00805f9b34fb"] = "GYROSCOPE_DATA"
        ATTRIBUTES["0000aa52-0000-1000-8000-00805f9b34fb"] = "GYROSCOPE_CONF"
        ATTRIBUTES["0000aa60-0000-1000-8000-00805f9b34fb"] = "TEST_SERV"
        ATTRIBUTES["0000aa61-0000-1000-8000-00805f9b34fb"] = "TEST_DATA"
        ATTRIBUTES["0000aa62-0000-1000-8000-00805f9b34fb"] = "TEST_CONF"
        ATTRIBUTES["0000ffe0-0000-1000-8000-00805f9b34fb"] = "SK Service"
        ATTRIBUTES["0000ffe1-0000-1000-8000-00805f9b34fb"] = "SK_KEYPRESSED"
        ATTRIBUTES["0000ffa0-0000-1000-8000-00805f9b34fb"] = "Accelerometer Service"
        ATTRIBUTES["0000ffa1-0000-1000-8000-00805f9b34fb"] = "ACCEL_ENABLER"
        ATTRIBUTES["0000ffa2-0000-1000-8000-00805f9b34fb"] = "ACCEL_RANGE"
        ATTRIBUTES["0000ffa3-0000-1000-8000-00805f9b34fb"] = "ACCEL_X"
        ATTRIBUTES["0000ffa4-0000-1000-8000-00805f9b34fb"] = "ACCEL_Y"
        ATTRIBUTES["0000ffa5-0000-1000-8000-00805f9b34fb"] = "ACCEL_Z"
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 公开方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 通过UUID字符串获取服务名称
     */
    fun getServiceUuidName(uuidStr: String): String {
        return ATTRIBUTES[uuidStr] ?: "Unknown Services"
    }

    /**
     * 通过UUID字符串获取特征名称
     */
    fun getCharacteristicsUuidName(uuidStr: String): String {
        return ATTRIBUTES[uuidStr] ?: "Unknown Characteristics"
    }

    /**
     * 获取设备的AD结构列表
     *
     * @return AD结构列表
     */
    fun getAdvertiseRecords(scanRecord: ScanRecord): ArrayList<AdvertiseStruct> {
        val parseScanRecord = parseScanRecord(scanRecord.bytes)
        val advertiseStructs = ArrayList<AdvertiseStruct>()
        val entries = parseScanRecord.entries
        for (entry in entries) {
            val type = entry.key
            val data: ByteArray = entry.value
            val length = data.size + 1
            val advertiseRecord = AdvertiseStruct(length, type, data)
            advertiseStructs.add(advertiseRecord)
        }
        return advertiseStructs
    }

    /**
     * 获取全部服务信息的列表
     */
    fun getServiceDataInfoList(scanRecord: ScanRecord): ArrayList<ServiceDataInfo> {
        val result = ArrayList<ServiceDataInfo>()
        val serviceData = scanRecord.serviceData ?: return result
        if (serviceData.isEmpty()) {
            return result
        }
        for (data in serviceData) {
            result.add(ServiceDataInfo(data.key, data.value))
        }
        return result
    }


    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     * 私有方法
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 将完整的广播包解析成AdRecord集合
     *
     * @param scanRecordBytes 完整的广播包
     */
    private fun parseScanRecord(scanRecordBytes: ByteArray): HashMap<Byte, ByteArray> {
        val bleSparseArray = HashMap<Byte, ByteArray>()
        var length: Byte
        var index = 0
        while (index < scanRecordBytes.size) {
            length = scanRecordBytes[index++]
            if (length.toInt() == 0) {
                break
            }
            val type = scanRecordBytes[index].toInt()
            if (type == 0) {
                break
            }
            val data = scanRecordBytes.copyOfRange(index + 1, index + length)
            bleSparseArray[(type and 0xFF).toByte()] = data
            index += length.toInt()
        }
        return bleSparseArray
    }
}
package com.openapp.openappsdk.kotlin

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import co.openapp.openappsdk.extension.toast
import co.openapp.sdk.OpenAppClient
import co.openapp.sdk.OpenAppService
import co.openapp.sdk.callback.*
import co.openapp.sdk.data.api.Status
import co.openapp.sdk.utils.Constants
import com.openapp.openappsdk.R
import com.ttlock.bl.sdk.entity.LockError
import kotlinx.android.synthetic.main.activity_doorlock.*
import timber.log.Timber

class DoorLockActivity : AppCompatActivity() {

    private lateinit var mService: OpenAppService

    //PadLock : C8:DF:84:2B:97:0D,
    //Door Lock: D9:5A:FC:92:35:8A, D6:D4:9A:AF:02:87 (Dev Test), D5:15:D5:92:5E:87 (Door Dev Test)
    private var deviceMac = "F0:07:EE:03:03:77"

    private var addedFingerprintNum = 0L
    private var icCardNum = 0L

    private var passcode: String? = "19237"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doorlock)

        OpenAppClient.create(application)
        mService = OpenAppService.getService(application, true)

        mService.fetchOALocks(
                "1e888j000000000",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJvcGVuYXBwIiwidXNlcklkIjoiMWU4ODhuZDAwMDAwMDAwIiwidXNlck1ldGEiOnsiaWQiOiIxZTg4OG5kMDAwMDAwMDAiLCJidXNpbmVzcyI6W3siaWQiOiIxZXUwaTg0MDAwMDAwMDAiLCJhdXRoVXNlcklkIjoiMWU4ODhuZDAwMDAwMDAwIiwib3JnYW5pc2F0aW9uSWQiOiIxZTg4OGowMDAwMDAwMDAiLCJjb21wYW55SWQiOiIxZTg4OGowMDAwMDAwMDAiLCJzdWJDb21wYW55SWQiOiIxZXJyMmgzMDAwMDAwMDAiLCJyb2xlIjoiMWVycjJoMzAwMDAwMDAxIn0seyJpZCI6IjFlODg4bmQwMDAwMDAwMCIsImF1dGhVc2VySWQiOiIxZTg4OG5kMDAwMDAwMDAiLCJvcmdhbmlzYXRpb25JZCI6IjFlODg4ajAwMDAwMDAwMCIsImNvbXBhbnlJZCI6IjFlODg4ajAwMDAwMDAwMCIsInN1YkNvbXBhbnlJZCI6IjFlODg4ajAwMDAwMDAwMCIsInJvbGUiOiIxZTg4OGowMDAwMDAwMDAifV19LCJjbGllbnQiOnt9LCJleHAiOjE2MjM3NDM3MzQsImlhdCI6MTYxODU1OTczNH0.AmybmZ199rOcJcKZ12AxeZCelxqJUDfczWsRixO8bEA"
        )

        mService.loadOALocks().observe(this, Observer { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    Timber.e(result.data.toString())
                    mService.startScan()
                }
                Status.ERROR -> {
                }//Error
                Status.LOADING -> {
                }//On Progress
            }
        })

        /**
         * this should be called first,to make sure Bluetooth configuration is ready.
         */
        mService.prepareDoorService(this)

        btn_unlock.setOnClickListener {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (ContextCompat.checkSelfPermission(
                                this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                ) {
//                    mService.startOperateLock(this, getDeviceMac())

                    Timber.e("Logs - log")

                    mService.getDoorLockLogs(
                            getDeviceMac(),
                            object : OAGetOperationLogCallback {

                                override fun onGetLogSuccess(log: String?) {
                                    Timber.e("Logs - $log")
                                }

                                override fun onFail(error: LockError?) {
                                    Timber.e(error?.description)
                                }
                            })
                }
            }
        }

        btn_addfingerprint.setOnClickListener {
            mService.addFingerPrint(0, 0, getDeviceMac(), object : OAAddFingerprintCallback {
                override fun onEnterAddMode(totalCount: Int) {
                    Timber.e("Total Count - $totalCount")
                    toast("Enter Add Fingerprint count $totalCount")
                }

                override fun onCollectFingerprint(currentCount: Int) {
                    Timber.e("Current Count - $currentCount")
                    toast("Collect Fingerprint count $currentCount")
                }

                override fun onAddFingerpintFinished(fingerprintNum: Long) {
                    Timber.e("FingerPrint Finished")
                    addedFingerprintNum = fingerprintNum
                    toast("Add Fingerprint Success $addedFingerprintNum")
                }

                override fun onFail(error: LockError?) {
                    Timber.e(error?.description)
                    toast(error?.description)
                }
            })
        }

        btn_modifyfingerprint.setOnClickListener {
            mService.modifyFingerprintValidityPeriod(0,
                    0,
                    addedFingerprintNum.toString(),
                    getDeviceMac(),
                    getKeyId(),
                    object : OAModifyFingerprintPeriodCallback {

                        override fun onFail(error: LockError?) {
                            Timber.e(error?.description)
                            toast(error?.description)
                        }

                        override fun onModifyPeriodSuccess() {
                            Timber.e("FingerPrint onModifyPeriodSuccess")
                            toast("Modify Fingerprint Success $addedFingerprintNum")
                        }
                    })
        }

        btn_deletefingerprint.setOnClickListener {
            mService.deleteFingerPrint(addedFingerprintNum.toString(), getDeviceMac(), getKeyId(),
                    object : OADeleteFingerprintCallback {

                        override fun onFail(error: LockError?) {
                            Timber.e(error?.description)
                            toast(error?.description)
                        }

                        override fun onDeleteFingerprintSuccess() {
                            Timber.e("FingerPrint onDeleteFingerprintSuccess")
                            addedFingerprintNum = 0L
                            toast("Delete Fingerprint Success $addedFingerprintNum")
                        }

                    })
        }

        btn_deleteallfingerprint.setOnClickListener {
            mService.clearAllFingerPrints(getDeviceMac(),
                    object : OAClearAllFingerprintCallback {
                        override fun onFail(error: LockError?) {
                            Timber.e(error?.description)
                            toast(error?.description)
                        }

                        override fun onClearAllFingerprintSuccess() {
                            Timber.e("FingerPrint onClearAllFingerprintSuccess")
                            addedFingerprintNum = 0L
                            toast("Clear All Fingerprint Success $addedFingerprintNum")
                        }
                    })
        }

        btn_addiccard.setOnClickListener {
            mService.addICCard(0, 0, getDeviceMac(),
                    object : OAAddICCardCallback {
                        override fun onFail(error: LockError?) {
                            Timber.e(error?.description)
                            toast(error?.description)
                        }

                        override fun onAddICCardSuccess(cardNum: Long) {
                            Timber.e("ICCard onAddICCardSuccess")
                            icCardNum = cardNum
                            toast("Add ICCard Success $icCardNum")
                        }

                        override fun onEnterAddMode() {
                            Timber.e("ICCard onEnterAddMode")
                            toast("Enter Add ICCard Mode $icCardNum")
                        }
                    })
        }

        btn_modifyiccard.setOnClickListener {
            mService.modifyICCardValidityPeriod(0, 0, icCardNum.toString(), getDeviceMac(), getKeyId(),
                    object : OAModifyICCardPeriodCallback {
                        override fun onFail(error: LockError?) {
                            Timber.e(error?.description)
                            toast(error?.description)
                        }

                        override fun onModifyICCardPeriodSuccess() {
                            Timber.e("ICCard onModifyICCardPeriodSuccess")
                            toast("Modify ICCard Success $icCardNum")
                        }
                    })
        }

        btn_deleteiccard.setOnClickListener {
            mService.deleteICCard(icCardNum.toString(), getDeviceMac(), getKeyId(),
                    object : OADeleteICCardCallback {
                        override fun onDeleteICCardSuccess() {
                            Timber.e("ICCard onDeleteICCardSuccess")
                            icCardNum = 0L
                            toast("Delete ICCard Success $icCardNum")
                        }

                        override fun onFail(error: LockError?) {
                            Timber.e(error?.description)
                            toast(error?.description)
                        }
                    })
        }

        btn_deletealliccard.setOnClickListener {
            mService.clearAllICCards(getDeviceMac(),
                    object : OAClearAllICCardCallback {
                        override fun onClearAllICCardSuccess() {
                            Timber.e("ICCard onClearAllICCardSuccess")
                            icCardNum = 0L
                            toast("Clear All ICCard Success $icCardNum")
                        }

                        override fun onFail(error: LockError?) {
                            Timber.e(error?.description)
                            toast(error?.description)
                        }
                    })
        }

        //default passcode
        btn_addpasscode.setOnClickListener {

            val currentTime = System.currentTimeMillis();

            mService.addCustomPasscode(passcode,
                    currentTime,
                    (currentTime * (1000 * 60 * 60 * 10)),
                    getDeviceMac(),
                    object : OACreateCustomPasscodeCallback {
                        override fun onCreateCustomPasscodeSuccess(passcodeStr: String?) {
                            Timber.e("ICCard onCreateCustomPasscodeSuccess")
                            passcode = passcodeStr
                            toast("Custom Passcode Success $passcode")
                        }

                        override fun onFail(error: LockError?) {
                            Timber.e(error?.description)
                            toast(error?.description)
                        }
                    })
        }

        btn_modifypasscode.setOnClickListener {
            mService.modifyCustomPasscode(passcode, 0, 0, getDeviceMac(), getKeyId(),
                    object : OAModifyPasscodeCallback {
                        override fun onFail(error: LockError?) {
                            Timber.e(error?.description)
                            toast(error?.description)
                        }

                        override fun onModifyPasscodeSuccess() {
                            Timber.e("ICCard onModifyPasscodeSuccess")
                            toast("Modify Passcode Success $passcode")
                        }
                    })
        }

        btn_deletepasscode.setOnClickListener {
            mService.deletePasscode(passcode, getDeviceMac(), getKeyId(),
                    object : OADeletePasscodeCallback {
                        override fun onFail(error: LockError?) {
                            Timber.e(error?.description)
                            toast(error?.description)
                        }

                        override fun onDeletePasscodeSuccess() {
                            Timber.e("ICCard onModifyPasscodeSuccess")
                            resetPassCode()
                            toast("Delete Passcode Success $passcode")
                        }
                    })
        }

        btn_deleteallpasscode.setOnClickListener {
            mService.clearAllPasscodes(getDeviceMac(),
                    object : OAResetPasscodeCallback {
                        override fun onFail(error: LockError?) {
                            Timber.e(error?.description)
                            toast(error?.description)
                        }

                        override fun onResetPasscodeSuccess(pwdInfo: String) {
                            Timber.e("ICCard onResetPasscodeSuccess")
                            resetPassCode()
                            toast("Reset Passcode Success $passcode")
                        }
                    })
        }

    }

    private fun getDeviceMac(): String {
        val deviceMacAdd = et_device_mac.text.toString()
        return if (deviceMacAdd.isEmpty())
            deviceMac
        else
            deviceMacAdd
    }

    private fun getKeyId(): String {
        val keyId = et_key_id.text.toString()
        return if (keyId.isEmpty())
            ""
        else
            keyId
    }

    private fun resetPassCode() {
        passcode = "19237"
    }

    override fun onDestroy() {
        super.onDestroy()
        mService.stopDoorService()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.e(requestCode.toString())
        if (requestCode == Constants.REQUEST_CODE) {


            if (resultCode == Activity.RESULT_OK) {

                if (data != null) {
                    val hashMap =
                            data.getSerializableExtra(Constants.LOCK_STATUS_MAP) as HashMap<Long, Boolean>

                    Timber.e(hashMap.toString())
                }
            }
        }
    }
}
package com.openapp.openappsdk.kotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import co.openapp.sdk.OALockCallBack
import co.openapp.sdk.OpenAppClient
import co.openapp.sdk.OpenAppService
import co.openapp.sdk.data.api.Status
import co.openapp.sdk.data.db.entity.Lock
import com.openapp.openappsdk.LockAdapter
import com.openapp.openappsdk.R
import kotlinx.android.synthetic.main.activity_chooser.*

class ChooserActivity : AppCompatActivity() {

    val TAG = ChooserActivity::class.java.name

    private lateinit var mService: OpenAppService

    private val lockList: MutableList<Lock> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chooser)

        val listAdapter = LockAdapter { lock ->
            // Use your mac address here
            lock?.macAddress?.let {
//                if (it.equals("C8:DF:84:2B:97:0D", true))
                if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@let
                }
                mService.startOperateLock(this, "C8:DF:84:2B:97:0D")
            }

            Log.e(TAG, lock.toString())
        }
        rv_lock_list.layoutManager = LinearLayoutManager(this)
        rv_lock_list.adapter = listAdapter


        mService = OpenAppService.getService(application, false)

        mService.loadOALocks().observe(this, Observer { response ->
            if (response != null) {
                when (response.status) {
                    Status.SUCCESS -> Log.e(TAG, "SUCCESS ${response.data?.message} - ${response.data}")
                    Status.ERROR -> Log.e(TAG, "ERROR ${response.message} ${response.data?.message}")
                    Status.LOADING -> Log.e(TAG, "Loading")
                }
            }
        })

        // scan the lock which user has access
        mService.getOALocks(object : OALockCallBack {
            override fun onLockState(state: OpenAppClient.State?) {
                Log.e(TAG, "Lock State - ${state?.name}")
            }

            override fun onError(errorMessage: String?) {
                Log.e(TAG, "Lock Error Message - $errorMessage")
            }

        }).observe(this, Observer { lock ->
            if (lock != null && lock.macAddress != null) {
                Log.e(TAG, "Lock - ${lock.macAddress} - ${lock.lockName} - ${lock.aesKey} - ${lock.lockId} - ${lock.lockType}")
                lockList.add(lock)
                listAdapter.submitList(lockList)
            }
        })

        start_scan.setOnClickListener { mService.startOperateLock(this, "C8:DF:84:2B:97:0D") }
//        start_scan.setOnClickListener { startActivity(Intent(this, DoorLockActivity::class.java)) }
//        start_scan.setOnClickListener { mService.startScan() }

        stop_scan.setOnClickListener { mService.stopScan() }
    }

    override fun onResume() {
        super.onResume()

        // fetch the lock from api and save it in local db
        fetchLock()
    }

    private fun fetchLock() {
        lockList.clear()

        mService.fetchOALocks(
                "1e888j000000000",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJvcGVuYXBwIiwidXNlcklkIjoiMWVpcHJwNTAwMDAwMDAwIiwidXNlck1ldGEiOnsiaWQiOiIxZWlwcnA1MDAwMDAwMDAiLCJidXNpbmVzcyI6W3siaWQiOiIxZWlwcnA1MDAwMDAwMDAiLCJhdXRoVXNlcklkIjoiMWVpcHJwNTAwMDAwMDAwIiwib3JnYW5pc2F0aW9uSWQiOiIxZTg4OGpqMDAwMDAwMDAiLCJjb21wYW55SWQiOiIxZTg4OGpqMDAwMDAwMDEiLCJzdWJDb21wYW55SWQiOiIxZThhdTN0MDAwMDAwMDAiLCJyb2xlIjoiMWU4YmU0ejAwMDAwMDAxIn0seyJpZCI6IjFlODg4bmQwMDAwMDAwMCIsImF1dGhVc2VySWQiOiIxZWlwcnA1MDAwMDAwMDAiLCJvcmdhbmlzYXRpb25JZCI6IjFlODg4ajAwMDAwMDAwMCIsImNvbXBhbnlJZCI6IjFlODg4ajAwMDAwMDAwMCIsInN1YkNvbXBhbnlJZCI6IjFlODg4ajAwMDAwMDAwMCIsInJvbGUiOiIxZTg4OGowMDAwMDAwMDAifV19LCJjbGllbnQiOnt9LCJleHAiOjE2MjQ2ODEyODcsImlhdCI6MTYxOTQ5NzI4N30.Rhb22745ljoxpVVHB1OKpvbVdk2kTYZwWWX-LxeoXHc"
        )
    }
}
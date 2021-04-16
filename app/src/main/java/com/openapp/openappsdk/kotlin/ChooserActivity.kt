package com.openapp.openappsdk.kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
                if (it.equals("C8:DF:84:2B:97:0D", true))
                    mService.startOperateLock(this, it)
            }

            Log.e(TAG, lock.toString())
        }
        rv_lock_list.layoutManager = LinearLayoutManager(this)
        rv_lock_list.adapter = listAdapter


        mService = OpenAppService.getService(application, true)

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
                Log.e(TAG, "Lock - ${lock.macAddress} - ${lock.lockName} - ${lock.aesKey}")
                lockList.add(lock)
                listAdapter.submitList(lockList)
            }
        })

//        start_scan.setOnClickListener { mService.startOperateLock(this, "C8:DF:84:2B:97:0D") }
        start_scan.setOnClickListener { startActivity(Intent(this, DoorLockActivity::class.java)) }

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
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJvcGVuYXBwIiwiZXhwIjoxNTYyOTI2NzUwMjgzLCJ1c2VySWQiOiIxZWlwcnA1MDAwMDAwMDAiLCJ1c2VyTWV0YSI6eyJpZCI6IjFlaXBycDUwMDAwMDAwMCIsImJ1c2luZXNzIjpbeyJpZCI6IjFlODg4bmQwMDAwMDAwMCIsImF1dGhVc2VySWQiOiIxZWlwcnA1MDAwMDAwMDAiLCJvcmdhbmlzYXRpb25JZCI6IjFlODg4ajAwMDAwMDAwMCIsImNvbXBhbnlJZCI6IjFlODg4ajAwMDAwMDAwMCIsInN1YkNvbXBhbnlJZCI6IjFlODg4ajAwMDAwMDAwMCIsInJvbGUiOiIxZTg4OGowMDAwMDAwMDAifSx7ImlkIjoiMWVpcHJwNTAwMDAwMDAwIiwiYXV0aFVzZXJJZCI6IjFlaXBycDUwMDAwMDAwMCIsIm9yZ2FuaXNhdGlvbklkIjoiMWU4ODhqajAwMDAwMDAwIiwiY29tcGFueUlkIjoiMWU4ODhqajAwMDAwMDAxIiwic3ViQ29tcGFueUlkIjoiMWU4YXUzdDAwMDAwMDAwIiwicm9sZSI6IjFlOGJlNHowMDAwMDAwMSJ9XX0sImNsaWVudCI6e30sImlhdCI6MTU2Mjg0MDM1MH0.VanDq0AsTbVO5E-8n8Ubo9JqNp0ElVzOIYnUwrLlTiA"
        )
    }
}
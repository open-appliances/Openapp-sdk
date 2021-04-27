package com.openapp.openappsdk.java;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.openapp.openappsdk.LockAdapter;
import com.openapp.openappsdk.R;

import java.util.ArrayList;

import co.openapp.sdk.OALockCallBack;
import co.openapp.sdk.OpenAppClient;
import co.openapp.sdk.OpenAppService;
import co.openapp.sdk.data.db.entity.Lock;

public class ChooserActivity extends AppCompatActivity {

    private final String TAG = ChooserActivity.class.getName();

    private OpenAppService mService;

    private ArrayList<Lock> lockList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chooser);

        Button startScan = findViewById(R.id.start_scan);
        Button stopScan = findViewById(R.id.stop_scan);
        RecyclerView rvLockList = findViewById(R.id.rv_lock_list);

        LockAdapter lockAdapter = new LockAdapter((Lock lock) -> {
            // Use your mac address here
            if (lock != null && lock.getMacAddress().equalsIgnoreCase("C8:DF:84:2B:97:0D")) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);
                    }
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return null;
                }
                mService.startOperateLock(this, lock.getMacAddress());
            }
            return null;
        });
        rvLockList.setLayoutManager(new LinearLayoutManager(this));
        rvLockList.setAdapter(lockAdapter);

        mService = OpenAppService.getService(getApplication(), false);

        mService.loadOALocks().observe(this, response -> {
            if (response != null) {
                switch (response.getStatus()) {
                    case SUCCESS:
                        Log.e(TAG, "SUCCESS " + response.data.getMessage() + " - " + response.data);
                        break;
                    case ERROR:
                        Log.e(TAG, "ERROR " + response.getMessage() + " - ");
                        break;
                    case LOADING:
                        Log.e(TAG, "Loading");
                        break;
                }
            }
        });

        // scan the lock which user has access
        mService.getOALocks(new OALockCallBack() {
            @Override
            public void onLockState(OpenAppClient.State state) {
                Log.e(TAG, "Lock State - " + state.name());
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Lock Error Message - " + errorMessage);
            }
        }).observe(this, lock -> {
            if (lock != null && lock.getMacAddress() != null) {
                Log.e(TAG, "Lock - " + lock.getMacAddress() + " - " + lock.getLockName() + " - " + lock.getLockId());
                lockList.add(lock);
                lockAdapter.submitList(lockList);
            }
        });

//        startScan.setOnClickListener(v -> mService.startScan());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startScan.setOnClickListener(v -> mService.startScan());

        stopScan.setOnClickListener(v -> mService.stopScan());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // fetch the lock from api and save it in local db
        fetchLock();
    }

    private void fetchLock() {
        lockList.clear();

        mService.fetchOALocks(
                "1e888j000000000",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJvcGVuYXBwIiwidXNlcklkIjoiMWVpcHJwNTAwMDAwMDAwIiwidXNlck1ldGEiOnsiaWQiOiIxZWlwcnA1MDAwMDAwMDAiLCJidXNpbmVzcyI6W3siaWQiOiIxZWlwcnA1MDAwMDAwMDAiLCJhdXRoVXNlcklkIjoiMWVpcHJwNTAwMDAwMDAwIiwib3JnYW5pc2F0aW9uSWQiOiIxZTg4OGpqMDAwMDAwMDAiLCJjb21wYW55SWQiOiIxZTg4OGpqMDAwMDAwMDEiLCJzdWJDb21wYW55SWQiOiIxZThhdTN0MDAwMDAwMDAiLCJyb2xlIjoiMWU4YmU0ejAwMDAwMDAxIn0seyJpZCI6IjFlODg4bmQwMDAwMDAwMCIsImF1dGhVc2VySWQiOiIxZWlwcnA1MDAwMDAwMDAiLCJvcmdhbmlzYXRpb25JZCI6IjFlODg4ajAwMDAwMDAwMCIsImNvbXBhbnlJZCI6IjFlODg4ajAwMDAwMDAwMCIsInN1YkNvbXBhbnlJZCI6IjFlODg4ajAwMDAwMDAwMCIsInJvbGUiOiIxZTg4OGowMDAwMDAwMDAifV19LCJjbGllbnQiOnt9LCJleHAiOjE2MjQ2ODEyODcsImlhdCI6MTYxOTQ5NzI4N30.Rhb22745ljoxpVVHB1OKpvbVdk2kTYZwWWX-LxeoXHc"
        );
    }
}

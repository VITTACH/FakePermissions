package com.vittach.sample.utils

import androidx.appcompat.app.AppCompatActivity
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.coroutinespermission.PermissionManager

class PermissionsHelper {

    private var activity: AppCompatActivity? = null

    fun attach(activity: AppCompatActivity) {
        this.activity = activity
    }

    fun detach() {
        activity = null
    }

    suspend fun requestPermission(vararg permissionName: String): PermissionResult {
        if (activity == null) {
            throw IllegalStateException("PermissionHelper is not attached to Activity")
        }

        var permissionResult: PermissionResult
        do {
            permissionResult = PermissionManager.requestPermissions(activity!!, 0, *permissionName)
        } while (permissionResult is PermissionResult.ShowRational)
        return permissionResult
    }

    suspend fun isPermissionGranted(permissionName: String): Boolean {
        val result = requestPermission(permissionName)
        return result is PermissionResult.PermissionGranted
    }
}
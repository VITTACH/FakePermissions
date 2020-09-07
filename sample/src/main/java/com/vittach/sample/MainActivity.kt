package com.vittach.sample

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.vittach.fakepermission.PermissionActivity
import com.vittach.fakepermission.PermissionActivity.Companion.ACCENT_COLOR
import com.vittach.fakepermission.PermissionActivity.Companion.LAND_BOTTOM_MARGINS
import com.vittach.fakepermission.PermissionActivity.Companion.PORTRAIT_BOTTOM_MARGINS
import com.vittach.fakepermission.PermissionActivity.Companion.FAKE_ICONS
import com.vittach.fakepermission.PermissionActivity.Companion.FAKE_PERMISSIONS
import com.vittach.fakepermission.PermissionActivity.Companion.FONT_FAMILY
import com.vittach.fakepermission.PermissionActivity.Companion.LAND_SIDE_MARGINS
import com.vittach.fakepermission.PermissionActivity.Companion.ORIGIN_PERMISSIONS
import com.vittach.fakepermission.PermissionActivity.Companion.PORTRAIT_SIDE_MARGINS
import com.vittach.fakepermission.PermissionActivity.Companion.TEXT_COLOR
import com.vittach.fakepermission.pxFromDp
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.CALL_PHONE
        )

        permissionButton.setOnClickListener {
            ActivityCompat.requestPermissions(this, permissions, 1)
            showFakePermissions(permissions)
        }
    }

    private fun showFakePermissions(originPermissions: Array<String>) {

        val fakePermissions = arrayOf(
            getString(R.string.permission_fine_location_fake),
            getString(R.string.permission_body_sensors_fake),
            getString(R.string.permission_camera_fake),
            getString(R.string.permission_read_calendar_fake),
            getString(R.string.permission_read_contacts_fake),
            getString(R.string.permission_read_sms_fake),
            getString(R.string.permission_record_audio_fake),
            getString(R.string.permission_write_call_log_fake),
            getString(R.string.permission_call_phone_fake)
        )

        val fakeIcons = arrayOf(
            R.drawable.ic_location,
            R.drawable.ic_anchor,
            R.drawable.ic_location,
            R.drawable.ic_anchor,
            R.drawable.ic_location,
            R.drawable.ic_anchor
        )

        val portraitBottomMargins = arrayOf(
            50f.pxFromDp(this),
            50f.pxFromDp(this),
            32f.pxFromDp(this),
            32f.pxFromDp(this),
            32f.pxFromDp(this),
            50f.pxFromDp(this),
            32f.pxFromDp(this),
            32f.pxFromDp(this),
            52f.pxFromDp(this)
        )

        val landBottomMargins = arrayOf(
            38f.pxFromDp(this),
            38f.pxFromDp(this),
            20f.pxFromDp(this),
            20f.pxFromDp(this),
            20f.pxFromDp(this),
            38f.pxFromDp(this),
            20f.pxFromDp(this),
            20f.pxFromDp(this),
            38f.pxFromDp(this)
        )

        val portraitSideMargins = arrayOf(
            28.5f.pxFromDp(this)
        )

        val landSideMargin = 149f.pxFromDp(this)
        val landSideMargins = arrayOf(
            landSideMargin,
            landSideMargin,
            landSideMargin,
            landSideMargin,
            landSideMargin,
            landSideMargin,
            154.5f.pxFromDp(this),
            landSideMargin
        )

        startActivity(
            Intent(this, PermissionActivity::class.java)
                .apply {
                    putExtra(ACCENT_COLOR, getColor(R.color.accentColor))
                    putExtra(TEXT_COLOR, getColor(R.color.textColor))
                    putExtra(FONT_FAMILY, "sans-serif-medium")
                    putExtra(ORIGIN_PERMISSIONS, originPermissions)
                    putExtra(FAKE_PERMISSIONS, fakePermissions)
                    putExtra(FAKE_ICONS, fakeIcons)
                    putExtra(PORTRAIT_BOTTOM_MARGINS, portraitBottomMargins)
                    putExtra(PORTRAIT_SIDE_MARGINS, portraitSideMargins)
                    putExtra(LAND_BOTTOM_MARGINS, landBottomMargins)
                    putExtra(LAND_SIDE_MARGINS, landSideMargins)
                }
        )
    }
}

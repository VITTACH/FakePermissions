package com.vittach.fakepermission

import android.Manifest
import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Global.WINDOW_ANIMATION_SCALE
import android.provider.Settings.Global.getFloat
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import kotlinx.android.synthetic.main.activity_permission.*
import kotlin.math.max
import kotlin.math.min


class PermissionActivity : AppCompatActivity() {

    private lateinit var appName: String

    companion object {
        private const val TRANSLATE_START_DELAY: Long = 210
        private const val TRANSLATE_Y_LENGTH: Long = 650
        private const val TRANSLATE_X_LENGTH: Long = 500
        private const val SCALE_XY_LENGTH: Long = 500

        private const val SMALL_DIALOG_HEIGHT = 140f

        const val PORTRAIT_BOTTOM_MARGINS = "PORTRAIT_BOTTOM_MARGINS"
        const val LAND_BOTTOM_MARGINS = "LAND_BOTTOM_MARGINS"

        const val PORTRAIT_SIDE_MARGINS = "PORTRAIT_SIDE_MARGINS"
        const val LAND_SIDE_MARGINS = "LAND_SIDE_MARGINS"

        const val ORIGIN_PERMISSIONS = "ORIGIN_PERMISSIONS"
        const val FAKE_PERMISSIONS = "FAKE_PERMISSIONS"

        const val FAKE_ICONS = "FAKE_ICONS"

        const val TEXT_COLOR = "TEXT_COLOR"
        const val ACCENT_COLOR = "ACCENT_COLOR"
        const val FONT_FAMILY = "FONT_FAMILY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        // прокидвание нажатия скозь активити
        window.addFlags(FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCH_MODAL or FLAG_NOT_TOUCHABLE)

        appName = applicationInfo.loadLabel(packageManager).toString()

        startInitAnimation()
        startWatchDog()
    }

    private fun startWatchDog() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val initActivitiesNum = activityManager.appTasks[0].taskInfo.numActivities

        val portraitBottomMargins =
            intent.extras?.getSerializable(PORTRAIT_BOTTOM_MARGINS) as? Array<Int>
                ?: arrayOf(50f.pxFromDp(this))
        val landBottomMargins =
            intent.extras?.getSerializable(LAND_BOTTOM_MARGINS) as? Array<Int>
                ?: arrayOf(38f.pxFromDp(this))
        val portraitSideMargins =
            intent.extras?.getSerializable(PORTRAIT_SIDE_MARGINS) as? Array<Int>
                ?: arrayOf(28.5f.pxFromDp(this))
        val landSideMargins =
            intent.extras?.getSerializable(LAND_SIDE_MARGINS) as? Array<Int>
                ?: arrayOf(149f.pxFromDp(this))

        val originPermissions = intent.extras?.getStringArray(ORIGIN_PERMISSIONS) ?: emptyArray()
        val originResources = originPermissions.toStringArray()
        val fakePermissions = intent.extras?.getStringArray(FAKE_PERMISSIONS) ?: emptyArray()
        val fakeIcons = intent.extras?.getSerializable(FAKE_ICONS) as? Array<Int> ?: emptyArray()

        val textColor = intent.extras?.getInt(TEXT_COLOR) ?: getColor(R.color.textColor)
        val accentColor = intent.extras?.getInt(ACCENT_COLOR) ?: getColor(R.color.accentColor)
        val fontFamily = intent.extras?.getString(FONT_FAMILY) ?: "sans-serif-medium"

        var newIndex = 0
        while (isPermissionGranted(originPermissions[newIndex])) {
            newIndex++
            if (newIndex == originPermissions.size) {
                finish()
                return // All permissions was granted
            }
        }
        var oldIndex = newIndex

        changeDialogHeight()
        changePermission(
            false,
            newIndex,
            landSideMargins,
            landBottomMargins,
            portraitSideMargins,
            portraitBottomMargins,
            textColor,
            fontFamily,
            accentColor,
            fakeIcons,
            originResources[newIndex],
            fakePermissions[newIndex]
        )

        val permissionStatus = mutableListOf<Boolean>()
        originPermissions.forEach { permission ->
            permissionStatus.add(isPermissionGranted(permission))
        }


        class WatchDog : AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg p0: Unit) {
                while (true) {
                    if (activityManager.appTasks[0].taskInfo.numActivities < initActivitiesNum) {
                        finish()
                        break
                    }

                    for (k in originPermissions.indices) {
                        val isWasGranted = isPermissionGranted(originPermissions[k])
                        if (permissionStatus[k] != isWasGranted) {
                            permissionStatus[k] = isWasGranted
                            newIndex = k + 1
                            break
                        }
                    }

                    if (newIndex != oldIndex && newIndex < originPermissions.size) {
                        oldIndex = newIndex
                        publishProgress()
                    }
                }
            }

            override fun onProgressUpdate(vararg values: Unit) {
                super.onProgressUpdate(*values)
                changePermission(
                    true,
                    newIndex,
                    landSideMargins,
                    landBottomMargins,
                    portraitSideMargins,
                    portraitBottomMargins,
                    textColor,
                    fontFamily,
                    accentColor,
                    fakeIcons,
                    originResources[newIndex],
                    fakePermissions[newIndex]
                )
            }
        }

        WatchDog().execute()
    }

    private fun startInitAnimation() {
        val duration = getFloat(contentResolver, WINDOW_ANIMATION_SCALE, 1.0f)

        dialogContainer.translationY = 20f.pxFromDp(this).toFloat()
        dialogContainer.animate()
            .setStartDelay(TRANSLATE_START_DELAY)
            .setDuration((duration * TRANSLATE_Y_LENGTH).toLong())
            .translationY(0f)
            .start()
    }

    private fun changePermission(
        hasAnimation: Boolean,
        i: Int,
        landSideMargins: Array<Int>,
        landBottomMargins: Array<Int>,
        portraitSideMargins: Array<Int>,
        portraitBottomMargins: Array<Int>,
        textColor: Int,
        fontFamily: String,
        accentColor: Int,
        fakeIcons: Array<Int>,
        originPermission: String,
        fakePermission: String
    ) {
        val landSideMargin = landSideMargins[min(i, landSideMargins.size - 1)]
        val portraitSideMargin = portraitSideMargins[min(i, portraitSideMargins.size - 1)]
        val landBottomMargin = landBottomMargins[min(i, landBottomMargins.size - 1)]
        val portraitBottomMargin = portraitBottomMargins[min(i, portraitBottomMargins.size - 1)]
        val fakeIcon = fakeIcons.getOrNull(min(i, max(fakeIcons.size - 1, 0)))
        val duration = getFloat(contentResolver, WINDOW_ANIMATION_SCALE, 1.0f)
        val fontColor = Integer.toHexString(textColor).substring(2)
        val fontTypeFace = Typeface.create(fontFamily, Typeface.NORMAL)

        ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(accentColor))

        val header = getString(R.string.permission_header)
        val formattedText = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            "$header $appName $fakePermission"
        } else {
            Html.fromHtml("<font color=#$fontColor>$header</font> $appName <font color=#$fontColor>$fakePermission</font>")
        }
        origTextView.text = "$header $appName $originPermission"
        fakeTextView.text = formattedText
        origTextView.typeface = fontTypeFace
        fakeTextView.typeface = fontTypeFace

        val layoutParams = dialogContainer.layoutParams as ViewGroup.MarginLayoutParams

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.leftMargin = landSideMargin
            layoutParams.rightMargin = landSideMargin
            layoutParams.bottomMargin = landBottomMargin
        } else {
            layoutParams.leftMargin = portraitSideMargin
            layoutParams.rightMargin = portraitSideMargin
            layoutParams.bottomMargin = portraitBottomMargin
        }

        dialogContainer.layoutParams = layoutParams

        if (hasAnimation) {
            fakeTextView.translationX = fakeTextView.width.toFloat()
            fakeTextView.animate()
                .setStartDelay(TRANSLATE_START_DELAY)
                .setDuration((duration * TRANSLATE_X_LENGTH).toLong())
                .translationX(0f)
                .start()

            fakeIcon?.let {
                icon.visibility = View.VISIBLE
                icon.animate()
                    .setStartDelay(TRANSLATE_START_DELAY)
                    .setDuration((duration * SCALE_XY_LENGTH).toLong())
                    .scaleX(0f)
                    .scaleY(0f)
                    .setListener(object : AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}
                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationRepeat(animation: Animator) {}
                        override fun onAnimationEnd(animation: Animator) {
                            icon.setImageResource(fakeIcon)
                            icon.scaleX = 1f
                            icon.scaleY = 1f
                        }
                    })
                    .start()
            } ?: run { icon.visibility = View.INVISIBLE }
        } else {
            fakeIcon?.let {
                icon.visibility = View.VISIBLE
                icon.setImageResource(fakeIcon)
            } ?: run { icon.visibility = View.INVISIBLE }
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
    }

    private fun changeDialogHeight() {
        dialogContainer.visibility = View.VISIBLE
        dialogContainer.layoutParams.height = SMALL_DIALOG_HEIGHT.pxFromDp(this)
    }

    private fun Array<String>.toStringArray() = this.map {
        val permissionString = when (it) {
            Manifest.permission.ACCESS_FINE_LOCATION -> getString(R.string.permission_fine_location_origin)
            Manifest.permission.BODY_SENSORS -> getString(R.string.permission_body_sensors)
            Manifest.permission.CAMERA -> getString(R.string.permission_camera)
            Manifest.permission.READ_CALENDAR -> getString(R.string.permission_read_calendar)
            Manifest.permission.READ_CONTACTS -> getString(R.string.permission_read_contacts)
            Manifest.permission.READ_SMS -> getString(R.string.permission_read_sms)
            Manifest.permission.RECORD_AUDIO -> getString(R.string.permission_record_audio)
            Manifest.permission.WRITE_CALL_LOG -> getString(R.string.permission_write_call_log)
            Manifest.permission.CALL_PHONE -> getString(R.string.permission_call_phone_origin)
            else -> ""
        }
        "${getString(R.string.permission_header)} $appName $permissionString"
    }.toTypedArray()
}

fun Float.pxFromDp(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

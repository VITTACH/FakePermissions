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
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import kotlinx.android.synthetic.main.activity_permission.*
import kotlin.math.max
import kotlin.math.min


class PermissionActivity : AppCompatActivity() {

    private lateinit var appName: String

    private var watchdogTask: AsyncTask<Unit, Unit, Unit>? = null

    companion object {
        private const val TRANSLATE_START_DELAY: Long = 210
        private const val TRANSLATE_Y_LENGTH: Long = 650
        private const val TRANSLATE_X_LENGTH: Long = 500
        private const val SCALE_XY_LENGTH: Long = 500

        const val PORTRAIT_BOTTOM_MARGINS = "PORTRAIT_BOTTOM_MARGINS"
        const val LAND_BOTTOM_MARGINS = "LAND_BOTTOM_MARGINS"

        const val PORTRAIT_WIDTHS = "PORTRAIT_WIDTHS"
        const val LAND_WIDTHS = "LAND_WIDTHS"

        const val ORIGIN_PERMISSIONS = "ORIGIN_PERMISSIONS"
        const val FAKE_PERMISSIONS = "FAKE_PERMISSIONS"

        const val FAKE_ICONS = "FAKE_ICONS"

        const val TEXT_COLOR = "TEXT_COLOR"
        const val ACCENT_COLOR = "ACCENT_COLOR"
        const val FONT_FAMILY = "FONT_FAMILY"

        const val FIRST_SHOWN = "FIRST_SHOWN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        window.addFlags(FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCH_MODAL or FLAG_NOT_TOUCHABLE)

        appName = applicationInfo.loadLabel(packageManager).toString()

        startInitAnimation()
        startWatchDog()
    }

    override fun onDestroy() {
        watchdogTask?.cancel(true)
        super.onDestroy()
    }

    private fun isTablet(): Boolean {
        val xlarge =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK === Configuration.SCREENLAYOUT_SIZE_XLARGE
        val large =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK === Configuration.SCREENLAYOUT_SIZE_LARGE
        return xlarge || large
    }

    private fun startWatchDog() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val initActivitiesNum = activityManager.appTasks[0].taskInfo.numActivities

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        val portraitBottomMargins =
            (intent.extras?.getSerializable(PORTRAIT_BOTTOM_MARGINS) as? Array<Int>)
                ?.map { it + getBasePortraitBottom() }
                ?: listOf(getBasePortraitBottom())
        val landBottomMargins = (intent.extras?.getSerializable(LAND_BOTTOM_MARGINS) as? Array<Int>)
            ?.map { it + getBaseLandBottom() }
            ?: listOf(getBaseLandBottom())

        val portraitWidths = (intent.extras?.getSerializable(PORTRAIT_WIDTHS) as? Array<Int>)
            ?.map { it + getBasePortraitWidth(screenWidth) }
            ?: listOf(getBasePortraitWidth(screenWidth))
        val landWidths = (intent.extras?.getSerializable(LAND_WIDTHS) as? Array<Int>)
            ?.map { it + getBaseLandWidth(screenWidth) }
            ?: listOf(getBaseLandWidth(screenWidth))

        intent.extras?.getBoolean(FIRST_SHOWN, true)?.let { isFirstShown ->
            do_not_ask_checkbox.visibility = if (isFirstShown) View.GONE else View.INVISIBLE
        }

        val originPermissions = intent.extras?.getStringArray(ORIGIN_PERMISSIONS) ?: emptyArray()
        val originResources = originPermissions.toStringArray()
        val fakePermissions = intent.extras?.getStringArray(FAKE_PERMISSIONS) ?: emptyArray()
        val fakeIcons = intent.extras?.getSerializable(FAKE_ICONS) as? Array<Int> ?: emptyArray()

        val textColor = (intent.extras?.getInt(TEXT_COLOR) ?: getColor(R.color.textColor)).let {
            Integer.toHexString(it).substring(2)
        }

        intent.extras?.getInt(ACCENT_COLOR)?.let {
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(it))
        }

        intent.extras?.getString(FONT_FAMILY)?.let {
            val fontTypeFace = Typeface.create(it, Typeface.NORMAL)
            fakeTextView.typeface = fontTypeFace
        }

        var newIndex = 0
        while (isPermissionGranted(originPermissions[newIndex])) {
            newIndex++
            if (newIndex == originPermissions.size) {
                finish()
                return // All permissions was granted
            }
        }
        var oldIndex = newIndex

        rootView.visibility = View.VISIBLE
        changePermission(
            false,
            newIndex,
            landWidths,
            landBottomMargins,
            portraitWidths,
            portraitBottomMargins,
            textColor,
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
                    landWidths,
                    landBottomMargins,
                    portraitWidths,
                    portraitBottomMargins,
                    textColor,
                    fakeIcons,
                    originResources[newIndex],
                    fakePermissions[newIndex]
                )
            }
        }

        watchdogTask = WatchDog().execute()
    }

    private fun startInitAnimation() {
        val duration = getFloat(contentResolver, WINDOW_ANIMATION_SCALE, 1.0f)

        rootView.translationY = 20f.pxFromDp(this).toFloat()
        rootView.animate()
            .setStartDelay(TRANSLATE_START_DELAY)
            .setDuration((duration * TRANSLATE_Y_LENGTH).toLong())
            .translationY(0f)
            .start()
    }

    private fun changePermission(
        hasAnimation: Boolean,
        i: Int,
        landWidths: List<Int>,
        landBottomMargins: List<Int>,
        portraitWidths: List<Int>,
        portraitBottomMargins: List<Int>,
        textColor: String,
        fakeIcons: Array<Int>,
        formattedOrigin: String,
        fakePermission: String
    ) {
        val landBottomMargin = landBottomMargins[min(i, landBottomMargins.size - 1)]
        val portraitBottomMargin = portraitBottomMargins[min(i, portraitBottomMargins.size - 1)]
        val fakeIcon = fakeIcons.getOrNull(min(i, max(fakeIcons.size - 1, 0)))
        val duration = getFloat(contentResolver, WINDOW_ANIMATION_SCALE, 1.0f)

        val header = getString(R.string.permission_header)
        val formattedFake = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            "$header \"$appName\" $fakePermission"
        } else {
            Html.fromHtml("<font color=#$textColor>$header</font> $appName <font color=#$textColor>$fakePermission</font>")
        }

        fakeTextView.text = formattedOrigin
        fakeTextView.post {
            val originHeight = fakeTextView.measuredHeight
            fakeTextView.text = formattedFake
            fakeTextView.layoutParams.height = originHeight
        }

        val marginLayoutParams = rootView.layoutParams as ViewGroup.MarginLayoutParams
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            marginLayoutParams.bottomMargin = landBottomMargin
            marginLayoutParams.width = landWidths[min(i, landWidths.size - 1)]
        } else {
            marginLayoutParams.bottomMargin = portraitBottomMargin
            marginLayoutParams.width = portraitWidths[min(i, portraitWidths.size - 1)]
        }
        rootView.layoutParams = marginLayoutParams

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

    private fun getBasePortraitWidth(width: Int): Int {
        return ((if (isTablet()) 6f / 9f else 16f / 17f) * width).toInt() - 32f.pxFromDp(this)
    }

    private fun getBaseLandWidth(width: Int): Int {
        return ((if (isTablet()) 4f / 8f else 4f / 6f) * width).toInt() - 32f.pxFromDp(this)
    }

    private fun getBasePortraitBottom(): Int {
        return (if (isTablet()) 48f else 28f).pxFromDp(this)
    }

    private fun getBaseLandBottom(): Int {
        return (if (isTablet()) 44f else 24f).pxFromDp(this)
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
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
        "${getString(R.string.permission_header)} \"$appName\" $permissionString"
    }.toTypedArray()
}

fun Float.pxFromDp(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

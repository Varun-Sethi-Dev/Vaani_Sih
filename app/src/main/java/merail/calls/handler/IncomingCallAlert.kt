package merail.calls.handler

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class IncomingCallAlert {

    companion object {
        private const val WINDOW_WIDTH_RATIO = 0.98f
    }

    private lateinit var windowManager: WindowManager

    private var windowLayout: ViewGroup? = null

    private var params = WindowManager.LayoutParams(
        // width
        WindowManager.LayoutParams.WRAP_CONTENT,
        // height
        WindowManager.LayoutParams.WRAP_CONTENT,
        // type
        windowType,
        // flags
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        // format
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.CENTER
        format = 1
    }

    private var x = 0f

    private var y = 0f

    private val windowType: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

    private val WindowManager.windowWidth: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = currentWindowMetrics
            val insets = windowMetrics.getWindowInsets()
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            (WINDOW_WIDTH_RATIO * (windowMetrics.bounds.width() - insets.left - insets.right)).toInt()
        } else {
            DisplayMetrics().apply {
                defaultDisplay?.getMetrics(this)
            }.run {
                (WINDOW_WIDTH_RATIO * widthPixels).toInt()
            }
        }

    fun showWindow(context: Context, phone: String) {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowLayout = View.inflate(context, R.layout.window_call_info, null) as ViewGroup?
        windowLayout?.let {
            params.width = windowManager.windowWidth


            val btnStart: Button = it.findViewById(R.id.btnStart)
            val btnClose: Button = it.findViewById(R.id.btnClose)
            val conversationBox: LinearLayout = it.findViewById(R.id.conversationBox)
            val conversationText: TextView = it.findViewById(R.id.conversationText)

            btnStart.setOnClickListener {
                if (conversationBox.visibility == View.GONE) {
                    conversationBox.visibility = View.VISIBLE
                    conversationText.text = "Translation starting...\n" +
                            "Translation in progress... \n\n" +
                            "Hi\n" +
                            "Harry this side"
                    btnStart.text = "Stop"
                } else {
                    conversationBox.visibility = View.GONE
                    btnStart.text = "Start"

                }
            }
            btnClose.setOnClickListener {
                closeWindow()
            }


            windowManager.addView(it, params)
            setOnTouchListener()
        }

    }


    fun closeWindow() {
        if (windowLayout != null) {
            windowManager.removeView(windowLayout)
            windowLayout = null
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListener() {
        windowLayout?.setOnTouchListener { view: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX
                    y = event.rawY
                }

                MotionEvent.ACTION_MOVE -> updateWindowLayoutParams(event)
                MotionEvent.ACTION_UP -> view.performClick()
                else -> Unit
            }
            false
        }
    }

    private fun updateWindowLayoutParams(event: MotionEvent) {
        params.x -= (x - event.rawX).toInt()
        params.y -= (y - event.rawY).toInt()
        windowManager.updateViewLayout(windowLayout, params)
        x = event.rawX
        y = event.rawY
    }

    @Composable
    fun CallInfoWindow(phone: String, onCancel: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Phone number: $phone")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
        }
    }
}
package com.miruken.mvc.android.databinding

import android.view.View
import androidx.databinding.DataBindingComponent
import android.os.SystemClock
import android.widget.ImageView

object DataBindingConventions : DataBindingComponent {
    override fun getClickBindings() = ClickingBindingsAdapter

    object ClickingBindingsAdapter : ClickBindings {
        override fun bindClick(view: View, listener: View.OnClickListener) =
                view.setOnClickListener(DebounceClickListener(listener,
                DEFAULT_DEBOUNCE_INTERVAL))
    }

    class DebounceClickListener(
            private val clickListener:    View.OnClickListener,
            private val debounceInterval: Long = DEFAULT_DEBOUNCE_INTERVAL
    ) : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(view: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceInterval) {
                return
            }
            lastClickTime = SystemClock.elapsedRealtime()
            clickListener.onClick(view)
        }
    }
}

private const val DEFAULT_DEBOUNCE_INTERVAL = 500L
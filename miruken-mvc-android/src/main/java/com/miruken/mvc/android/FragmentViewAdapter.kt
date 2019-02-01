package com.miruken.mvc.android

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.miruken.mvc.Navigation
import com.miruken.mvc.view.Viewing
import com.miruken.mvc.view.ViewingRegion

class FragmentViewAdapter(
        private val fragment: Fragment
) : Viewing, ViewProvider {
    override var viewModel: Any? = null

    override fun display(region: ViewingRegion) = region.show(this)

    override fun createView(
            context:    Context,
            parent:     ViewGroup,
            navigation: Navigation<*>?
    ): View {
        val activity = context as? FragmentActivity ?:
                error("Unable to obtain FragmentManager from context")
        val container = FrameLayout(context).apply {
            id = View.generateViewId()
        }
        activity.supportFragmentManager
                .beginTransaction()
                .replace(container.id, fragment)
                .commit()
        navigation?.context?.also {
            it.contextEnded += {
                activity.supportFragmentManager
                        .beginTransaction()
                        .remove(fragment)
                        .commit()
            }
        }
        return container
    }
}
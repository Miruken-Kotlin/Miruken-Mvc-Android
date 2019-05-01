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
        private val fragment: Fragment,
        private val stateless: Boolean = false
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
                .replace(container.id, fragment).let {
                    if (stateless) {
                        it.commitAllowingStateLoss()
                    } else {
                        it.commit()
                    }
                }
        navigation?.context?.also { ctx ->
            ctx.contextEnded += {
                activity.supportFragmentManager
                        .beginTransaction()
                        .remove(fragment).let {
                            if (stateless) {
                                it.commitAllowingStateLoss()
                            } else {
                                it.commit()
                            }
                        }
            }
        }
        return container
    }
}
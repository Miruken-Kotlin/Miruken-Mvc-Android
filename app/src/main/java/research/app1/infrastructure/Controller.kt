package research.app1.infrastructure

import android.animation.ObjectAnimator
import android.app.Activity
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.transition.Fade
import android.transition.Fade.IN
import android.transition.Fade.OUT
import android.transition.Scene
import android.transition.TransitionManager
import android.view.View

open class Controller {

    var views: MutableList<View> = mutableListOf()

    fun inflate(view: Int) : View {
        return activity.layoutInflater.inflate(view, null)
    }

    fun push(view: Int) : View {
        return push(inflate(view))
    }

    fun push(view: View) : View {
        animate(view)
        views.add(view)
        region.addView(view)
        return view
    }

    fun <T : ViewDataBinding?>push(view: Int) : T {
        return DataBindingUtil.bind<T>(push(view))
    }

    fun animate(view: View) : View {

        //With property animator
        val shortDuration = 5000L

        //out with the old view if it exists
        if(views.any()){
            views.last().animate().apply {
                alpha(0f)
                duration = shortDuration
                //setListener(null)
            }
        }
        //in with the new view
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate().apply {
            alpha(1f)
            duration = shortDuration
            //setListener(null)
        }

        //With Scene and Transition Manager
//        if(views.any()){
//            val scene = Scene(region, views.last())
//            TransitionManager.go(scene, Fade(OUT))
//        }

        //With ObjectAnimator
//        view.x = -500f
//        ObjectAnimator.ofFloat(view, "translationX", 0f).apply {
//            duration = 500
//            start()
//        }

        return view
    }

    companion object {
        lateinit var activity: Activity
        lateinit var region:   Region
    }
}
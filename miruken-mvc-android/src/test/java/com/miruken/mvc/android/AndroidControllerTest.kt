package com.miruken.mvc.android

import androidx.databinding.Bindable
import com.miruken.callback.Provides
import com.miruken.context.Scoped
import com.miruken.mvc.android.databinding.bindable
import org.junit.Test

class AndroidControllerTest {
    @Test fun `Creates Controller ViewModel`() {
    }

    class MyController
        @Provides @Scoped
        constructor(): AndroidController() {

        inner class MyViewModel : AndroidController.ViewModel() {
            @get:Bindable
            val title by bindable("Hello", 1)

            fun fetchData() {
            }
        }

        fun hello() {
            next<MyController> { hello() }
        }
    }
}
package com.miruken.mvc.android

import androidx.databinding.Bindable
import com.miruken.mvc.android.databinding.bindable
import org.junit.Test

class AndroidControllerTest {
    @Test fun `Creates Controller ViewModel`() {
    }

    class MyController : AndroidController() {
        inner class MyViewModel : ViewModel() {
            @get:Bindable
            val title by bindable("Hello", 1)

            fun fetchData() {
            }
        }
    }
}
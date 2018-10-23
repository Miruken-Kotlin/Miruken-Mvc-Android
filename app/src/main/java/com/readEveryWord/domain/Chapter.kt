package com.readEveryWord.domain

import android.databinding.BaseObservable
import android.databinding.Bindable
import com.readEveryWord.BR

class Chapter(val number: Int) : BaseObservable() {

    val id: Int = number - 1

    @Bindable
    var read: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.read)
            notifyPropertyChanged(BR.readState)
        }

    val numberText: String
        get() = number.toString()

    fun toggle(){
        read = !read
    }

    @Bindable
    var readState: ReadState = ReadState.NOT_STARTED
        get(){
            return when {
                read -> ReadState.COMPLETED
                else -> ReadState.NOT_STARTED
            }
        }
}

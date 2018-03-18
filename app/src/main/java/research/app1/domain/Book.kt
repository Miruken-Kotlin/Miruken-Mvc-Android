package research.app1.domain

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.Observable
import research.app1.BR
import research.app1.features.ReadState
import research.app1.features.calculateProgress

class Book(val id: Int, val longName: String, val shortName: String, val chapterCount: Int) : BaseObservable()
{
    val chapters: List<Chapter> = (1..chapterCount).map { Chapter(it) }

    private val chapterCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(observable: Observable, i: Int) {
            notifyPropertyChanged(BR.readState)
            notifyPropertyChanged(BR.progress)
        }
    }

    init {
        chapters.forEach{
            it.addOnPropertyChangedCallback(chapterCallback)
        }
    }

    val readCount: Int
        get() = chapters.filter { x -> x.read }.count()

    @Bindable
    var progress: String = ""
        get(){
            return calculateProgress(readCount, chapterCount)
        }

    @Bindable
    var readState: ReadState = ReadState.NOT_STARTED
        get(){
            return when {
                chapters.all { x -> x.read } -> ReadState.COMPLETED
                chapters.any { x -> x.read } -> ReadState.STARTED
                else                         -> ReadState.NOT_STARTED
            }
        }
}

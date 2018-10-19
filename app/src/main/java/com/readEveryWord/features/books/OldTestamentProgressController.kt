package com.readEveryWord.features.books

import com.android.databinding.library.baseAdapters.BR
import com.miruken.mvc.android.AndroidController
import com.readEveryWord.R
import com.readEveryWord.domain.Bible
import com.readEveryWord.domain.OldTestamentProgress

class OldTestamentProgressController : AndroidController() {

    lateinit var bible: Bible
    lateinit var progress: OldTestamentProgress

    fun showProgress(data: Bible){
        bible    = data
        progress = OldTestamentProgress(bible)
        show(R.layout.old_testament_progress, BR.ctrl)
    }

    fun pop (){

    }
}


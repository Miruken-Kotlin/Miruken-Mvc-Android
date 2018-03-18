package com.readEveryWord.domain

import com.readEveryWord.features.calculateProgress

class OldTestamentProgress(bible: Bible){
    val law:           String = calculateProgress(bible.law)
    val history:       String = calculateProgress(bible.history)
    val wisdom:        String = calculateProgress(bible.wisdom)
    val majorProphets: String = calculateProgress(bible.majorProphets)
    val minorProphets: String = calculateProgress(bible.minorProphets)
}

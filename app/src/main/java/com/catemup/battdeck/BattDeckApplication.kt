package com.catemup.battdeck

import android.app.Application
import com.catemup.battdeck.data.JsonRepository

class BattDeckApplication : Application() {
    val repository by lazy { JsonRepository(this) }
}

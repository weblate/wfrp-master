package cz.muni.fi.rpg.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.setContent
import cz.frantisekmasa.wfrp_master.core.ads.AdManager
import cz.muni.fi.rpg.ui.shell.*
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val adManager: AdManager by inject()

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProvideActivity(this) {
                WfrpMasterApp(adManager)
            }
        }
    }
}
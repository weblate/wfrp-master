package cz.muni.fi.rpg.di

import cz.muni.fi.rpg.ui.AuthenticatedActivity
import cz.muni.fi.rpg.ui.gameMaster.GameMasterActivity
import cz.muni.fi.rpg.ui.MainActivity
import cz.muni.fi.rpg.ui.partyList.PartyListActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesModule {
    @ContributesAndroidInjector
    abstract fun authenticatedActivity(): AuthenticatedActivity

    @ContributesAndroidInjector
    abstract fun mainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun partyListActivity(): PartyListActivity

    @ContributesAndroidInjector
    abstract fun gameMasterActivity(): GameMasterActivity
}
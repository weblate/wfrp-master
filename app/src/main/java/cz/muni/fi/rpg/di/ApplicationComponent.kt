package cz.muni.fi.rpg.di

import cz.muni.fi.rpg.Application
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        ActivitiesModule::class,
        FragmentModule::class,
        ModelModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<Application>
package com.dhyanu.moviehub.di

import com.dhyanu.moviehub.data.LocalMovieDataProvider
import com.dhyanu.moviehub.data.MovieDataProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindMovieDataProvider(
        localMovieDataProvider: LocalMovieDataProvider
    ): MovieDataProvider

}
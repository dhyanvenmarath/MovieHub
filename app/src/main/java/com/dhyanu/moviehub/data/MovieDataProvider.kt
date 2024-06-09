package com.dhyanu.moviehub.data

import com.dhyanu.moviehub.data.model.MoviesModel


interface MovieDataProvider {
    fun getMovies(pageNum: Int): MoviesModel?
}
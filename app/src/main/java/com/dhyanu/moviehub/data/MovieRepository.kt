package com.dhyanu.moviehub.data

import com.dhyanu.moviehub.data.model.MoviesModel
import javax.inject.Inject

class MovieRepository@Inject constructor(private val movieDataProvider: MovieDataProvider) {

    fun moviePagingSource(searchText: String = "", updateMovie: (MoviesModel) -> Unit) = MoviePagingSource(movieDataProvider, searchText, updateMovie)

}
package com.dhyanu.moviehub.data

import android.content.Context
import com.dhyanu.moviehub.data.model.MoviesModel
import com.dhyanu.moviehub.utils.AppConstant
import com.google.gson.Gson
import java.io.InputStream
import javax.inject.Inject

//This class handles the local data from the assets folder
class LocalMovieDataProvider  @Inject constructor(private val context: Context): MovieDataProvider {

    override fun getMovies(pageNum: Int): MoviesModel? {
        val stream: InputStream = when (pageNum) {
            1 -> context.assets.open(AppConstant.API_PAGE_1_FILE_NAME)
            2 -> context.assets.open(AppConstant.API_PAGE_2_FILE_NAME)
            3 -> context.assets.open(AppConstant.API_PAGE_3_FILE_NAME)
            else -> return null
        }
        val size = stream.available()
        val buffer = ByteArray(size)
        stream.read(buffer)
        stream.close()
        val movieJson = String(buffer)
        return Gson().fromJson(movieJson, MoviesModel::class.java)
    }

}
package com.dhyanu.moviehub.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dhyanu.moviehub.data.model.Content
import com.dhyanu.moviehub.data.model.MoviesModel
import com.google.gson.JsonSyntaxException
import java.io.IOException

private const val STARTING_PAGE_NUM = 1
class MoviePagingSource(private val movieDataProvider: MovieDataProvider, private val searchText: String = "", private val updateMovie: (MoviesModel) -> Unit) : PagingSource<Int, Content>() {

    override fun getRefreshKey(state: PagingState<Int, Content>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Content> {
        //This starts the paging with the first page if its the initial call
        val pageNum = params.key ?: STARTING_PAGE_NUM

        return try {
            val movie = movieDataProvider.getMovies(pageNum)
            val contentList : MutableList<Content> = mutableListOf()
            val nextKey = if ((movie == null) || movie.page.contentItems.content.isEmpty()) {
                null
            } else {
                pageNum + 1
            }
            movie?.let {
                updateMovie(it)
                if (searchText.length >= 3) {
                    for (content : Content in it.page.contentItems.content) {
                        if (content.name.contains(searchText, true))
                            contentList.add(content)
                    }
                } else {
                    contentList.addAll(it.page.contentItems.content)
                }
            }

            LoadResult.Page(
                data = contentList,
                prevKey = if (pageNum == STARTING_PAGE_NUM) null else pageNum - 1,
                nextKey = nextKey
            )
        }  catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: JsonSyntaxException) {
            return LoadResult.Error(exception)
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }
}
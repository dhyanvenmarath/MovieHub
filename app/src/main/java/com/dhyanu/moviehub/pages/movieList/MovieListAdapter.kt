package com.dhyanu.moviehub.pages.movieList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dhyanu.moviehub.data.model.Content
import com.dhyanu.moviehub.databinding.MovieListViewholderBinding

class MovieListAdapter : PagingDataAdapter<Content, MovieListViewHolder>(ARTICLE_DIFF_CALLBACK) {
    private var searchText: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieListViewHolder =
        MovieListViewHolder(
            MovieListViewholderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )

    override fun onBindViewHolder(holder: MovieListViewHolder, position: Int) {
        val tile = getItem(position)
        if (tile != null) {
            holder.bind(tile, searchText)
        }
    }

    companion object {
        private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Content>() {
            override fun areItemsTheSame(oldItem: Content, newItem: Content): Boolean =
                oldItem.name == newItem.name && oldItem.posterImage == newItem.posterImage

            override fun areContentsTheSame(oldItem: Content, newItem: Content): Boolean =
                oldItem == newItem
        }
    }

    fun updateSearchText(searchText: String) {
        this.searchText = searchText
    }

    fun clearSearch() {
        this.searchText = ""
    }
}
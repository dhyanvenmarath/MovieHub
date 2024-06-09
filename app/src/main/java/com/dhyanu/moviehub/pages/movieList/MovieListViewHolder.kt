package com.dhyanu.moviehub.pages.movieList

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.recyclerview.widget.RecyclerView
import com.dhyanu.moviehub.data.model.Content
import com.dhyanu.moviehub.databinding.MovieListViewholderBinding
import com.dhyanu.moviehub.utils.AppUtils.getImage

class MovieListViewHolder(
    private val binding: MovieListViewholderBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(content: Content, searchText: String) {
        binding.apply {
            if (searchText.isNotEmpty() && content.name.contains(searchText, true)) {
                val spannableName: Spannable = SpannableString(content.name)
                val startIndex = content.name.indexOf(searchText, ignoreCase = true)
                val stopIndex = startIndex + searchText.length
                spannableName.setSpan(
                    ForegroundColorSpan(Color.YELLOW),
                    startIndex,
                    stopIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                movieNameTV.text = spannableName
            } else {
                movieNameTV.text = content.name
            }

            val context = movieImageIV.context
            movieImageIV.setImageResource(
                getImage(
                    context,
                    content.posterImage.removeSuffix(".jpg")
                )
            )
        }
    }
}

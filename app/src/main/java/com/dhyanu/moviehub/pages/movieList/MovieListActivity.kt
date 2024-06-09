package com.dhyanu.moviehub.pages.movieList

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhyanu.moviehub.R
import com.dhyanu.moviehub.databinding.ActivityMovieListBinding
import com.dhyanu.moviehub.utils.AppConstant
import com.dhyanu.moviehub.utils.AppUtils.hideSoftKeyboard
import com.dhyanu.moviehub.utils.AppUtils.showSoftKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieListActivity : AppCompatActivity() {
    private val movieViewModel by viewModels<MovieListViewModel>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        val binding = ActivityMovieListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setUpToolbar(binding)

        // Get the view model

        val items = movieViewModel.pagingDataFlow
        val moiveListAdapter = MovieListAdapter()

        binding.bindState(
            movieViewModel.uiState,
            movieViewModel.accept,
            moiveListAdapter,
            resources.configuration.orientation
        )

        movieViewModel.uiState.distinctUntilChanged().observe(this@MovieListActivity) {
            when (it) {
                UiState.NormalUIState -> {
                    movieViewModel.accept(UiAction.ClearSearchResult)
                    hideSearchUI(binding)
                    moiveListAdapter.clearSearch()
                    moiveListAdapter.notifyDataSetChanged()
                }

                UiState.ShowingSearchUIState -> {
                    movieSearchUI(binding)
                    movieViewModel.accept(
                        UiAction.TypingSearchText(
                            binding.toolbar.searchET.text?.toString() ?: AppConstant.DEFAULT_QUERY
                        )
                    )
                }

                is UiState.TypingQueryState -> {
                    movieSearchUI(binding)
                    if (it.isSearching)
                        moiveListAdapter.updateSearchText(it.typedText)
                    else
                        moiveListAdapter.updateSearchText("")
                    moiveListAdapter.notifyDataSetChanged()
                }
            }

            movieViewModel.movieDataLivedata.distinctUntilChanged()
                .observe(this@MovieListActivity) { movieModel ->
                    binding.toolbar.titleTV.text = movieModel.page.title
                }
        }

        // Collect from the Article Flow in the ViewModel, and submit it to the
        // ListAdapter.
        lifecycleScope.launch {
            // We repeat on the STARTED lifecycle because an Activity may be PAUSED
            // but still visible on the screen, for example in a multi window app
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                items.collectLatest {
                    moiveListAdapter.submitData(it)
                }
            }
        }
    }

    private fun setUpToolbar(binding: ActivityMovieListBinding) {
        binding.toolbar.backArrowIV.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun hideSearchUI(binding: ActivityMovieListBinding) {
        binding.toolbar.searchET.visibility = View.GONE
        binding.toolbar.searchButtonIV.visibility = View.VISIBLE
        binding.toolbar.backArrowIV.visibility = View.VISIBLE
        binding.toolbar.titleTV.visibility = View.VISIBLE

        binding.toolbar.searchButtonIV.setImageResource(R.drawable.ic_search)
        hideSoftKeyboard(this@MovieListActivity, binding.toolbar.searchET)
    }

    private fun movieSearchUI(binding: ActivityMovieListBinding) {
        binding.toolbar.searchET.visibility = View.VISIBLE
        binding.toolbar.searchButtonIV.visibility = View.VISIBLE
        binding.toolbar.backArrowIV.visibility = View.GONE
        binding.toolbar.titleTV.visibility = View.GONE

        binding.toolbar.searchET.requestFocus()
        binding.toolbar.searchButtonIV.setImageResource(R.drawable.ic_search_cancel)
        showSoftKeyboard(this@MovieListActivity, binding.toolbar.searchET)
    }

}

private fun ActivityMovieListBinding.bindState(
    uiState: LiveData<UiState>,
    uiActions: (UiAction) -> Unit,
    movieListAdapter: MovieListAdapter,
    orientation: Int
) {
    bindAdapter(movieListAdapter, orientation)
    bindSearching(uiState, uiActions)
}

private fun ActivityMovieListBinding.bindAdapter(
    movieListAdapter: MovieListAdapter,
    orientation: Int
) {
    IdActivityMoviesRecycler.adapter = movieListAdapter
    val spanCount = if (orientation == Configuration.ORIENTATION_LANDSCAPE)
        AppConstant.LANDSCAPE_SPAN_COUNT
    else
        AppConstant.PORTRAIT_SPAN_COUNT
    IdActivityMoviesRecycler.layoutManager =
        GridLayoutManager(IdActivityMoviesRecycler.context, spanCount, RecyclerView.VERTICAL, false)
    val decoration = MovieCardSpaceDecoration(30, 90)
    IdActivityMoviesRecycler.addItemDecoration(decoration)
}


private fun ActivityMovieListBinding.bindSearching(
    uiState: LiveData<UiState>,
    onSearchAction: (UiAction) -> Unit
) {
    toolbar.searchButtonIV.setOnClickListener {
        onSearchAction(UiAction.UpdateSearchUIVisibility(uiState.value == UiState.NormalUIState))
    }

    toolbar.searchET.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            //When we rotate device then UI gets recreated and we get this callback with blank value witch trigger Search UI. So to fix this
            //we apply visibility condition
            if (toolbar.searchET.isVisible)
                onSearchAction(UiAction.TypingSearchText(s?.toString() ?: ""))
        }
    })
}



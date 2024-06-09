package com.dhyanu.moviehub.pages.movieList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dhyanu.moviehub.data.MovieRepository
import com.dhyanu.moviehub.data.model.Content
import com.dhyanu.moviehub.data.model.MoviesModel
import com.dhyanu.moviehub.utils.AppConstant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ITEMS_PER_PAGE = 20

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    val pagingDataFlow: Flow<PagingData<Content>>

    val uiState: LiveData<UiState>

    val accept: (UiAction) -> Unit

    val movieDataLivedata: LiveData<MoviesModel>

    private val updateMovie: (MoviesModel) -> Unit

    init {
        val uiStateMutable = MutableLiveData<UiState>(UiState.NormalUIState)
        val movieDataMutableLD = MutableLiveData<MoviesModel>()

        uiState = uiStateMutable
        movieDataLivedata = movieDataMutableLD

        val actionStateFlow = MutableSharedFlow<UiAction>()
        val searches = actionStateFlow
            .filterIsInstance<UiAction.TypingSearchText>()
            .distinctUntilChanged()
            .onStart {
                emit(UiAction.TypingSearchText(AppConstant.DEFAULT_QUERY))
            }
        pagingDataFlow = searches
            .distinctUntilChanged()
            .flatMapLatest {
                val searchText =
                    if (it.typeText.length >= AppConstant.NUM_CHAR_REQUIRED_TO_SEARCH)
                        it.typeText
                    else
                        AppConstant.DEFAULT_QUERY
                searchMovies(searchText = searchText)
            }
            .cachedIn(viewModelScope)

        accept = { action ->
            when (action) {
                is UiAction.UpdateSearchUIVisibility -> {
                    val state = if (action.movieSearchUI)
                        UiState.ShowingSearchUIState
                    else
                        UiState.NormalUIState
                    uiStateMutable.postValue(state)
                }

                UiAction.ClearSearchResult -> searchDefaultQuery(actionStateFlow)

                is UiAction.TypingSearchText -> {
                    val typeText = action.typeText
                    if (typeText.length >= AppConstant.NUM_CHAR_REQUIRED_TO_SEARCH) {
                        uiStateMutable.postValue(UiState.TypingQueryState(typeText, true))
                        viewModelScope.launch {
                            actionStateFlow.emit(action)
                        }
                    } else {
                        uiStateMutable.postValue(UiState.TypingQueryState(typeText, false))
                        searchDefaultQuery(actionStateFlow)
                    }
                }
            }
        }

        updateMovie = {
            movieDataMutableLD.postValue(it)
        }
    }

    private fun searchDefaultQuery(actionFlow: MutableSharedFlow<UiAction>) {
        viewModelScope.launch {
            actionFlow.emit(UiAction.TypingSearchText(AppConstant.DEFAULT_QUERY))
        }
    }


    private fun searchMovies(searchText: String = ""): Flow<PagingData<Content>> {
        return Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
            pagingSourceFactory = { movieRepository.moviePagingSource(searchText, updateMovie) }
        ).flow.cachedIn(viewModelScope)
    }

}

sealed class UiAction {
    data class TypingSearchText(val typeText: String) : UiAction()

    data class UpdateSearchUIVisibility(val movieSearchUI: Boolean) : UiAction()

    object ClearSearchResult : UiAction()
}


sealed class UiState {

    object NormalUIState : UiState()

    object ShowingSearchUIState : UiState()

    data class TypingQueryState(
        val typedText: String,
        val isSearching: Boolean
    ) : UiState()

}
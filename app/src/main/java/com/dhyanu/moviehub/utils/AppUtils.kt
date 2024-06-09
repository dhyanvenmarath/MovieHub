package com.dhyanu.moviehub.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.dhyanu.moviehub.R

object AppUtils {
    fun getImage(context: Context, imageName: String): Int {
        val resourceId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        return if (resourceId != 0)
            resourceId
        else
            R.drawable.placeholder_for_missing_posters
    }

    fun hideSoftKeyboard(context: Context, focusedView: View?) {
        focusedView?.let { view ->
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun showSoftKeyboard(context: Context, focusedView: View?) {
        focusedView?.let { _ ->
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(focusedView, 0)
        }
    }
}
package com.androiddevs.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.models.Article
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_article.*
import java.io.Serializable

class ArticleFragment: Fragment(R.layout.fragment_article) {
    lateinit var viewModel: NewsViewModel
    var arg: Article?= null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel= (activity as NewsActivity).viewModel

        arguments?.getSerializable("article")?.apply {
            Log.i("ArticleFragment",this.toString())
            arg= this as Article
        }
        webView.apply {
            webViewClient= WebViewClient()
            loadUrl(arg?.url)
        }

        fab.setOnClickListener {
            arg?.let { it -> viewModel.saveArticle(it) }
            Snackbar.make(view,"Saved the article",Snackbar.LENGTH_SHORT).show()
        }

    }
}
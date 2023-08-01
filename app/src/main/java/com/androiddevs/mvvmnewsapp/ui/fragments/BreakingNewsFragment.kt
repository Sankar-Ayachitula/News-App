package com.androiddevs.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.adapters.NewsAdapter
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.NewsViewModel
import com.androiddevs.mvvmnewsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.androiddevs.mvvmnewsapp.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_breaking_news.*

class BreakingNewsFragment: Fragment(R.layout.fragment_breaking_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel= (activity as NewsActivity).viewModel
        setupRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle= Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(R.id.action_breakingNewsFragment_to_articleFragment, bundle)
        }

        viewModel.breakingNews.observe(viewLifecycleOwner, Observer {resource->
            when(resource){
                is Resource.Loading -> {
                    showProgressBar()
                }
                is Resource.Success -> {
                    hideProgressBar()
                    resource.data?.let {
                        newsAdapter.differ.submitList(it.articles.toList())
                        val totalPages= it.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage= viewModel.breakingNewsPage == totalPages
                    }
                }
                is Resource.Error ->{
                    hideProgressBar()
                    Toast.makeText(context,"Error due to: ${resource.message}",Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun showProgressBar(){
        paginationProgressBar.visibility= View.VISIBLE
//        isLoading= true
    }

    fun hideProgressBar(){
        paginationProgressBar.visibility= View.GONE
//        isLoading= false
    }

    var isLoading= false
    var isLastPage= false
    var isScrolling= false

    val scrollListener= object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState== RecyclerView.SCROLL_STATE_DRAGGING){
                isScrolling= true
//                Log.i("HelloScroll","I am Scrolling")
//                val layoutManager= recyclerView.layoutManager as LinearLayoutManager
//                val firstVisibleItemPosition= layoutManager.findFirstVisibleItemPosition()
//                val visibleItemCount= layoutManager.childCount
//                val totalItemCount= layoutManager.itemCount
//                val isAtLastItem= firstVisibleItemPosition+ visibleItemCount >= totalItemCount
//
//                //condition for pagination
//                if(isAtLastItem && !isLastPage){
//                    viewModel.getBreakingNews("in")
//                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if(isScrolling){
                val layoutManager= recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition= layoutManager.findFirstVisibleItemPosition()
                val visibleItemCount= layoutManager.childCount
                val totalItemCount= layoutManager.itemCount
                val isAtLastItem= firstVisibleItemPosition+ visibleItemCount >= totalItemCount

                //condition for pagination
                if(isAtLastItem && !isLastPage){
                    viewModel.getBreakingNews("in")
                    isScrolling=false
                }
            }

//            val layoutManager= recyclerView.layoutManager as LinearLayoutManager
//            val firstVisibleItemPosition= layoutManager.findFirstVisibleItemPosition()
//            val visibleItemCount= layoutManager.childCount
//            val totalItemCount= layoutManager.itemCount
//
//            val isNotLoadingAndNotAtLastPage= !isLoading && !isLastPage
//            val isAtLastItem= firstVisibleItemPosition+ visibleItemCount >= totalItemCount
//            val isNotAtBeginning= firstVisibleItemPosition>=0
//            val isTotalMoreThanVisible= totalItemCount>= QUERY_PAGE_SIZE
//
//            val shouldPaginate= isNotLoadingAndNotAtLastPage && isAtLastItem && isNotAtBeginning &&
//                    isTotalMoreThanVisible && isScrolling
//            if(shouldPaginate){
//                viewModel.getBreakingNews("us")
//                isScrolling= false
//            }
        }
    }


    private fun setupRecyclerView(){
        newsAdapter= NewsAdapter()
        rvBreakingNews.apply {
            adapter= newsAdapter
            layoutManager= LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }
}
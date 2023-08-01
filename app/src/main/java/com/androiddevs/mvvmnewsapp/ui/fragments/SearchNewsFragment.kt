package com.androiddevs.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.adapters.NewsAdapter
import com.androiddevs.mvvmnewsapp.models.NewsResponse
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.NewsViewModel
import com.androiddevs.mvvmnewsapp.util.Constants
import com.androiddevs.mvvmnewsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.coroutines.*

class SearchNewsFragment: Fragment(R.layout.fragment_search_news) {
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
            findNavController().navigate(R.id.action_searchNewsFragment_to_articleFragment, bundle)
        }

        var job: Job?= null
        etSearch.addTextChangedListener {
            job?.cancel()
            job= lifecycleScope.launch{
                delay(500L)
                viewModel.searchNews.postValue(null)
                viewModel.searchNewsPage=1
                viewModel.searchNewsResponse= null
//                Log.i("Serachnews",viewModel.searchNews.value.toString())
//                Log.i("Serachnews",viewModel.searchNewsPage.toString())
//                Log.i("Serachnews",it.toString())
                viewModel.searchNews(it.toString())
            }
        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer {resource->
            when(resource){
                is Resource.Loading -> {
                    Log.i("Serachnews", "ikkadaki vastunnana?")
                    showProgressBar()
                }
                is Resource.Success -> {
                    hideProgressBar()
                    resource.data?.let {
                        newsAdapter.differ.submitList(it.articles.toList())
                        rvSearchNews.smoothScrollToPosition(0)

                        val totalPages= it.totalResults / Constants.SEARCH_PAGE_SIZE + 2
                        isLastPage= viewModel.searchNewsPage == totalPages
                    }
                }
                is Resource.Error ->{
                    hideProgressBar()
                    Log.i("ErrorMsg",resource.data.toString())
                    Toast.makeText(context,"Error due to: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun showProgressBar(){
        paginationProgressBar.visibility= View.VISIBLE
    }

    fun hideProgressBar(){
        paginationProgressBar.visibility= View.GONE
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
                    viewModel.searchNews(etSearch.text.toString())
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
        rvSearchNews.apply {
            adapter= newsAdapter
            layoutManager= LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }

}
package io.keepcoding.eh_ho.Posts

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.keepcoding.eh_ho.R
import io.keepcoding.eh_ho.data.PostsRepo
import io.keepcoding.eh_ho.data.RequestError
import kotlinx.android.synthetic.main.fragment_posts.*
import kotlinx.android.synthetic.main.fragment_posts.parentLayout


class PostsFragment : Fragment(){
    var listener: PostsInteractionListener? = null
    var idTopic: String? = ""
    lateinit var adapter: PostsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PostsInteractionListener)
            listener = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        adapter = PostsAdapter{
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_posts, menu)//Todo cambiar menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        idTopic = arguments?.getString("idTopic")
        return inflater.inflate(R.layout.fragment_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        listPosts.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        listPosts.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        listPosts.adapter = adapter

        swiperefreshPosts.setOnRefreshListener {
            idTopic?.let { loadPosts(it) }
            swiperefreshPosts.isRefreshing = false   // reset the SwipeRefreshLayout (stop the loading spinner)
        }
    }

    override fun onResume() {
        super.onResume()
        idTopic?.let { loadPosts(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_log_out -> listener?.onLogOut()
            R.id.action_create_post -> idTopic?.let { listener?.createPost(it) }
        }

        return super.onOptionsItemSelected(item)
    }

    fun loadPosts(idTopic:String) {
        enableLoading(true)
        context?.let {
            PostsRepo.getPosts(idTopic,it,//Todo pasar identificador topic
                {
                    enableLoading(false)
                    adapter.setPosts(it)
                },
                {
                    enableLoading(false)
                    handleRequestError(it)
                })
        }
    }

    private fun enableLoading(enabled: Boolean) {
        viewRetryPost.visibility = View.INVISIBLE

        if (enabled) {
            listPosts.visibility = View.INVISIBLE
            viewLoadingPost.visibility = View.VISIBLE
        } else {
            listPosts.visibility = View.VISIBLE
            viewLoadingPost.visibility = View.INVISIBLE
        }
    }

    private fun handleRequestError(requestError: RequestError) {
        listPosts.visibility = View.INVISIBLE
        viewRetryPost.visibility = View.VISIBLE

        val message = if (requestError.messageResId != null)
            getString(requestError.messageResId)
        else if (requestError.message != null)
            requestError.message
        else
            getString(R.string.error_request_default)

        Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG).show()
    }


    interface PostsInteractionListener {
        fun onLogOut()
        fun createPost(idTopic:String)
    }

}
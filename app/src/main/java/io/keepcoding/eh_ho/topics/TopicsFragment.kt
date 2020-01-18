package io.keepcoding.eh_ho.topics


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.keepcoding.eh_ho.R
import io.keepcoding.eh_ho.data.RequestError
import io.keepcoding.eh_ho.data.Topic
import io.keepcoding.eh_ho.data.TopicsRepo
import kotlinx.android.synthetic.main.fragment_topics.*
import kotlinx.android.synthetic.main.view_retry.*

class TopicsFragment : Fragment() {

    var listener: TopicsInteractionListener? = null
    lateinit var adapter: TopicsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TopicsInteractionListener)
            listener = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        adapter = TopicsAdapter{
            goToPosts(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_topics, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_topics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        listTopics.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        listTopics.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        listTopics.adapter = adapter

        buttonCreate.setOnClickListener {
            goToCreateTopic()
        }

        buttonRetry.setOnClickListener {
            loadTopics()
        }

        swiperefreshTopics.setOnRefreshListener {
            loadTopics()
            swiperefreshTopics.isRefreshing = false   // reset the SwipeRefreshLayout (stop the loading spinner)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTopics()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_log_out -> listener?.onLogOut()

        }

        return super.onOptionsItemSelected(item)
    }

    private fun loadTopics() {
        enableLoading(true)

        context?.let {
            TopicsRepo.getTopics(it,
                {
                    enableLoading(false)
                    adapter.setTopics(it)
                },
                {
                    enableLoading(false)
                    handleRequestError(it)
                })
        }
    }

    private fun enableLoading(enabled: Boolean) {
        viewRetry.visibility = View.INVISIBLE

        if (enabled) {
            listTopics.visibility = View.INVISIBLE
            buttonCreate.hide()
            viewLoading.visibility = View.VISIBLE
        } else {
            listTopics.visibility = View.VISIBLE
            buttonCreate.show()
            viewLoading.visibility = View.INVISIBLE
        }
    }

    private fun handleRequestError(requestError: RequestError) {
        listTopics.visibility = View.INVISIBLE
        viewRetry.visibility = View.VISIBLE

        val message = if (requestError.messageResId != null)
            getString(requestError.messageResId)
        else if (requestError.message != null)
            requestError.message
        else
            getString(R.string.error_request_default)

        Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG).show()
    }

    private fun goToCreateTopic() {
        listener?.onGoToCreateTopic()
    }

    private fun goToPosts(it: Topic) {
        listener?.onTopicSelected(it)
    }

    interface TopicsInteractionListener {
        fun onTopicSelected(topic: Topic)
        fun onGoToCreateTopic()
        fun onLogOut()
    }

}


class ScrollAwareFABBehavior(context: Context, attrs: AttributeSet): FloatingActionButton.Behavior(context, attrs) {

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout,
                                     child: FloatingActionButton, directTargetChild: View, target: View,
                                     axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout,
            child, directTargetChild, target, axes, type)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout,
                                child: FloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int,
                                dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
            dyUnconsumed, type)

        if (dyConsumed > 0 && child.visibility == View.VISIBLE) {
            child.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
                @SuppressLint("RestrictedApi")
                override fun onHidden(fab: FloatingActionButton) {
                    super.onHidden(fab)
                    fab.visibility = View.INVISIBLE
                }
            })
            // } else if (dyUnconsumed < 0 && child.visibility != View.VISIBLE) {
        } else if (dyConsumed < 0 && child.visibility != View.VISIBLE) {
            child.show()
        }
    }
}

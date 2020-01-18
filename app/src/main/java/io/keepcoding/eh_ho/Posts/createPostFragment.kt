package io.keepcoding.eh_ho.Posts

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import io.keepcoding.eh_ho.LoadingDialogFragment
import io.keepcoding.eh_ho.R
import io.keepcoding.eh_ho.data.*
import kotlinx.android.synthetic.main.fragment_create_post.*
import kotlinx.android.synthetic.main.fragment_create_topic.parentLayout

const val TAG_LOADING_DIALOG = "loading_dialog"

class CreatePostFragment : Fragment() {

    var listener: CreatePostInteractionListener? = null
    lateinit var loadingDialog: LoadingDialogFragment
    var idTopic: String? = ""
    var titleTopic: String? = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is CreatePostInteractionListener)
            listener = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        loadingDialog = LoadingDialogFragment.newInstance(getString(R.string.label_create_post))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        idTopic = arguments?.getString("idTopic")
        titleTopic = arguments?.getString(EXTRA_TOPIC_TITLE)
        return inflater.inflate(R.layout.fragment_create_post, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        titleTopicPost.setText(titleTopic)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_create_topic, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_send -> createPost()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun createPost() {

        if (isFormValid()) {
            idTopic?.let { sendPost(it) }
        } else {
            showErrors()
        }
    }

    private fun sendPost(idTopic:String) {
        val model = CreatePostModel(
            inputTitlePost.text.toString(),
            idTopic,
            inputRaw.text.toString()
        )

        context?.let {
            enableLoadingDialog(true)
            PostsRepo.createPost(
                it,
                model,
                {
                    enableLoadingDialog(false)
                    listener?.onPostCreated()
                },
                {
                    enableLoadingDialog(false)
                    handleError(it)
                }
            )
        }
    }

    private fun enableLoadingDialog(enabled: Boolean) {
        if (enabled)
            loadingDialog.show(childFragmentManager, TAG_LOADING_DIALOG)
        else
            loadingDialog.dismiss()
    }

    private fun handleError(requestError: RequestError) {
        val message = if (requestError.messageResId != null)
            getString(requestError.messageResId)
        else if (requestError.message != null)
            requestError.message
        else
            getString(R.string.error_request_default)

        Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showErrors() {
        if (inputTitlePost.text?.isEmpty() == true)
            inputTitlePost.error = getString(R.string.error_empty)
        if (inputRaw.text?.isEmpty() == true)
            inputRaw.error = getString(R.string.error_empty)
    }

    private fun isFormValid() =
        inputTitlePost.text?.isNotEmpty() ?: false && inputRaw.text?.isNotEmpty() ?:false

    interface CreatePostInteractionListener {
        fun onPostCreated()
    }
}
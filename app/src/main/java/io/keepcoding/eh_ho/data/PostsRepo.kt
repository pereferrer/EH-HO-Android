package io.keepcoding.eh_ho.data


import android.content.Context
import android.util.Log
import com.android.volley.NetworkError
import com.android.volley.Request
import com.android.volley.ServerError
import io.keepcoding.eh_ho.R
import org.json.JSONObject

object PostsRepo {

    fun getPosts(idPost:String,
                 context: Context,
                 onSuccess:(List<Post>) ->Unit,
                 onError:(RequestError)->Unit)
    {

        val username = UserRepo.getUsername(context)
        val request = UserRequest(
            username,
            Request.Method.GET,
            ApiRoutes.getPostsByTopicId(idPost),
            null,
            {
                it?.let {
                    onSuccess.invoke(Post.parsePosts(it))
                }

                if (it == null)
                    onError.invoke(RequestError(messageResId = R.string.error_invalid_response))
            },
            {
                it.printStackTrace()
                if (it is NetworkError)
                    onError.invoke(RequestError(messageResId = R.string.error_network))
                else
                    onError.invoke(RequestError(it))
            })

        ApiRequestQueue.getRequestQueue(context)
            .add(request)
    }

    fun createPost(
        context: Context,
        model: CreatePostModel,
        onSuccess: (CreatePostModel) -> Unit,
        onError: (RequestError) -> Unit
    ) {
        val username = UserRepo.getUsername(context)
        val request = UserRequest(
            username,
            Request.Method.POST,
            ApiRoutes.createTopic(),
            model.toJson(),
            {
                it?.let {
                    onSuccess.invoke(model)
                }

                if (it == null)
                    onError.invoke(RequestError(messageResId = R.string.error_invalid_response))
            },
            {
                it.printStackTrace()

                if (it is ServerError && it.networkResponse.statusCode == 422) {
                    val body = String(it.networkResponse.data, Charsets.UTF_8)
                    val jsonError = JSONObject(body)
                    val errors = jsonError.getJSONArray("errors")
                    var errorMessage = ""

                    for (i in 0 until errors.length()) {
                        errorMessage += "${errors[i]} "
                    }

                    onError.invoke(RequestError(it, message = errorMessage))

                } else if (it is NetworkError)
                    onError.invoke(RequestError(it, messageResId = R.string.error_network))
                else
                    onError.invoke(RequestError(it))
            }
        )

        ApiRequestQueue.getRequestQueue(context)
            .add(request)
    }
}
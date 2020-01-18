package io.keepcoding.eh_ho.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.android.volley.NetworkError
import com.android.volley.Request
import com.android.volley.ServerError
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import io.keepcoding.eh_ho.R
import java.lang.reflect.Method

const val PREFERENCES_SESSION = "session"
const val PREFERENCES_SESSION_USERNAME = "username"

object UserRepo {

    fun signIn(
        context: Context,
        signInModel: SignInModel,
        onSuccess: (SignInModel) -> Unit,
        onError: (RequestError) -> Unit
    ) {

        val request = AdminRequest(Request.Method.GET,
            ApiRoutes.signIn(signInModel.username),
            null,
            { response ->
                saveSession(context, signInModel.username)
                onSuccess.invoke(signInModel)
            },
            { error ->
                error.printStackTrace()

                if (error is ServerError && error.networkResponse.statusCode == 404)
                    onError.invoke(
                        RequestError(
                            error,
                            messageResId = R.string.error_not_registered
                        )
                    )
                else if (error is NetworkError)
                    onError.invoke(
                        RequestError(
                            error,
                            messageResId = R.string.error_network
                        )
                    )
                else
                    onError.invoke(RequestError(error))
            })

        ApiRequestQueue
            .getRequestQueue(context)
            .add(request)
    }

    fun signUp(
        context: Context,
        signUpModel: SignUpModel,
        onSuccess: (SignUpModel) -> Unit,
        onError: (RequestError) -> Unit
    ) {
        val request = AdminRequest(
            Request.Method.POST,
            ApiRoutes.signUp(),
            signUpModel.toJson(),
            { response ->

                response?.let {
                    if (it.optBoolean("success")) {
                        saveSession(context, signUpModel.username)
                        onSuccess.invoke(signUpModel)
                    } else
                        onError.invoke(RequestError(message = it.getString("message")))
                }

                if (response == null)
                    onError.invoke(RequestError(messageResId = R.string.error_invalid_response))

            },
            {
                it.printStackTrace()
                if (it is NetworkError)
                    onError.invoke(RequestError(it, messageResId = R.string.error_network))
                else
                    onError.invoke(RequestError(it))
            }
        )

        ApiRequestQueue.getRequestQueue(context)
            .add(request)
    }

    fun isLogged(context: Context): Boolean {
        val pref = context.getSharedPreferences(PREFERENCES_SESSION, MODE_PRIVATE)
        val user = pref.getString(PREFERENCES_SESSION_USERNAME, null)
        return user != null
    }

    fun getUsername(context: Context): String {
        val pref = context.getSharedPreferences(PREFERENCES_SESSION, MODE_PRIVATE)
        val user = pref.getString(PREFERENCES_SESSION_USERNAME, "") ?: ""

        return user
    }

    fun logOut(context: Context) {
        val pref = context.getSharedPreferences(PREFERENCES_SESSION, MODE_PRIVATE)
        pref.edit()
            .remove(PREFERENCES_SESSION_USERNAME)
            .apply()
    }

    private fun saveSession(context: Context, username: String) {
        val pref =
            context.applicationContext.getSharedPreferences(PREFERENCES_SESSION, MODE_PRIVATE)
        pref.edit()
            .putString(PREFERENCES_SESSION_USERNAME, username)
            .apply()
    }
}
package info.anodsplace.framework.playservices

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Tasks
import info.anodsplace.framework.app.ApplicationContext
import java.util.concurrent.ExecutionException

/**
 * @author Alex Gavrishev
 * @date 28/11/2017
 */
class GoogleSignInConnect(private val context: ApplicationContext, private val signInOptions: GoogleSignInOptions) {

    constructor(context: Context, signInOptions: GoogleSignInOptions): this(ApplicationContext(context), signInOptions)

    private fun createGoogleApiSignInClient(): GoogleSignInClient {

        return GoogleSignIn.getClient(context.actual, signInOptions)
    }

    interface Result {
        fun onSuccess(account: GoogleSignInAccount, client: GoogleSignInClient)
        fun onError(errorCode: Int, client: GoogleSignInClient)
    }

    @Throws(ApiException::class, ExecutionException::class, InterruptedException::class)
    fun connectLocked(): GoogleSignInAccount {
        val client = createGoogleApiSignInClient()
        return Tasks.await(client.silentSignIn())
    }

    fun connect(completion: Result) {
        val client = createGoogleApiSignInClient()

        val task = client.silentSignIn()
        if (task.isSuccessful) {
            completion.onSuccess(task.result, client)
        } else {
            task.addOnCompleteListener {
                try {
                    val signInAccount = task.getResult(ApiException::class.java)
                    completion.onSuccess(signInAccount, client)
                } catch (apiException: ApiException) {
                    // You can get from apiException.getStatusCode() the detailed error code
                    // e.g. GoogleSignInStatusCodes.SIGN_IN_REQUIRED means user needs to take
                    // explicit action to finish sign-in;
                    // Please refer to GoogleSignInStatusCodes Javadoc for details
                    completion.onError(apiException.statusCode, client)
                }
            }
        }
    }
}
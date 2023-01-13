package id.ac.istts.dkdm.myapiconnection

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.IOException

class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val mListener: Response.Listener<NetworkResponse>,
    private val mErrorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, mErrorListener) {

    private val mHeaders: MutableMap<String, String>
    private val mMultipartBody: MultipartBody

    init {
        mHeaders = HashMap()
        mMultipartBody = MultipartBody()
    }

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        return mHeaders
    }

    fun addHeader(key: String, value: String) {
        mHeaders[key] = value
    }

    fun addMultipartFile(key: String, file: ByteArray, fileName: String, mimeType: String) {
        mMultipartBody.addPart(key, file, fileName, mimeType)
    }

    fun addMultipartString(key: String, value: String) {
        mMultipartBody.addPart(key, value)
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray? {
        val bos = ByteArrayOutputStream()
        try {
            mMultipartBody.writeTo(bos)
        } catch (e: IOException) {
            Log.e(TAG, "IOException writing to ByteArrayOutputStream")
        }
        return bos.toByteArray()
    }

    override fun getBodyContentType(): String {
        return mMultipartBody.contentType
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: NetworkResponse) {
        mListener.onResponse(response)
    }

    companion object {
        private const val TAG = "VolleyMultipartRequest"
    }
}
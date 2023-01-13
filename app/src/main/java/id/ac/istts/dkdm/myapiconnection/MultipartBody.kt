package id.ac.istts.dkdm.myapiconnection

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MultipartBody {
    private val mBoundary: String
    private val mLineEnd = "\r\n"
    private val mParts: MutableList<Part>
    private val mOutputStream: ByteArrayOutputStream

    /**
     * Creates a new multipart body.
     */
    init {
        mBoundary = UUID.randomUUID().toString()
        mParts = ArrayList()
        mOutputStream = ByteArrayOutputStream()
    }

    /**
     * Gets the content type of the multipart body.
     *
     * @return The content type.
     */
    val contentType: String
        get() = "multipart/form-data; boundary=$mBoundary"

    /**
     * Adds a new part to the multipart body.
     *
     * @param key The name of the form field.
     * @param value The value of the form field.
     */
    fun addPart(key: String, value: String) {
        mParts.add(StringPart(key, value))
    }

    /**
     * Adds a new part to the multipart body.
     *
     * @param key The name of the form field.
     * @param file The file to add.
     * @param fileName The name of the file.
     * @param mimeType The mime type of the file.
     */
    fun addPart(key: String, file: ByteArray, fileName: String, mimeType: String) {
        mParts.add(ByteArrayPart(key, file, fileName, mimeType))
    }

    /**
     * Writes the multipart body to the given output stream.
     *
     * @param out The output stream to write to.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun writeTo(out: OutputStream) {
        for (part in mParts) {
            // Write the boundary
            out.write(("--$mBoundary$mLineEnd").toByteArray())

            // Write the headers
            for ((key, value) in part.headers) {
                out.write(("$key: $value$mLineEnd").toByteArray())
            }

            // Write the data
            out.write(mLineEnd.toByteArray())
            part.writeData(out)
            out.write(mLineEnd.toByteArray())
        }

        // End the multipart body
        out.write(("--$mBoundary--$mLineEnd").toByteArray())
    }

    /**
     * Represents a part in the multipart body.
     */
    private abstract class Part {
        internal val headers: MutableMap<String, String>

        init {
            headers = HashMap()
        }

        /**
         * Writes the data of the part to the given output stream.
         *
         * @param out The output stream to write to.
         * @throws IOException
         */
        @Throws(IOException::class)
        abstract fun writeData(out: OutputStream)
    }

    /**
     * Represents a string part in the multipart body.
     */
    private inner class StringPart(key: String, value: String) : Part() {
        init {
            headers["Content-Disposition"] = "form-data; name=$key"
            mOutputStream.write(value.toByteArray())
        }

        @Throws(IOException::class)
        override fun writeData(out: OutputStream) {
            out.write(mOutputStream.toByteArray())
        }
    }

    /**
     * Represents a byte array part in the multipart body.
     */
    private inner class ByteArrayPart(key: String, file: ByteArray, fileName: String, mimeType: String) : Part() {
        init {
            headers["Content-Disposition"] = "form-data; name=$key; filename=$fileName"
            headers["Content-Type"] = mimeType
            mOutputStream.write(file)
        }

        @Throws(IOException::class)
        override fun writeData(out: OutputStream) {
            out.write(mOutputStream.toByteArray())
        }
    }
}
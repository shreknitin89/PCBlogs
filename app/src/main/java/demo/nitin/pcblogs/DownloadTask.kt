package demo.nitin.pcblogs

import android.os.AsyncTask
import demo.nitin.pcblogs.DownloadTask.DownloadCompleteListener
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/** Implementation of AsyncTask used to download XML data and images from Personal Capital Blogs
 * @param listener - [DownloadCompleteListener] that provides whether the data was successfully
 * downloaded or not
 *
 */
class DownloadTask(private val listener: DownloadCompleteListener) :
    AsyncTask<String, Void, Unit>() {

    /**
     * Interface to notify the result to the corresponding callers
     * the listener needs to be passed as reference when implementing the task
     */
    interface DownloadCompleteListener {
        fun onContentDownloaded(inputStream: InputStream)
        fun onContentDownloadError(error: Throwable)
    }

    override fun doInBackground(vararg urls: String) {
        try {
            val inputStream = downloadUrl(urls[0])
            inputStream?.let { listener.onContentDownloaded(it) }
        } catch (e: IOException) {
            listener.onContentDownloadError(e)
        } catch (e: XmlPullParserException) {
            listener.onContentDownloadError(e)
        }
    }


    // Given a string representation of a URL, sets up a connection and gets an input stream.
    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream? {
        val url = URL(urlString)
        return (url.openConnection() as? HttpURLConnection)?.run {
            // Timeout for reading InputStream arbitrarily set to 15s.
            readTimeout = 15000
            // Timeout for connection.connect() arbitrarily set to 15s.
            connectTimeout = 15000
            // For this use case, set HTTP method to GET.
            requestMethod = "GET"
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            doInput = true
            // Starts the query
            // Open communications link (network traffic occurs here).
            connect()
            inputStream
        }
    }
}
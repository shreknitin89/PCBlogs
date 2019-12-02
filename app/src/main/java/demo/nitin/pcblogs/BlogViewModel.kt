package demo.nitin.pcblogs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.InputStream

private const val blogUrl = "https://www.personalcapital.com/blog/feed/"
private const val BLOGS = "BLOGS"

class BlogViewModel : ViewModel() {
    val cacheMap = HashMap<String, Any>()

    /**
     * Function returns the live data of the corresponding list of blogs directly by making a
     * network call using [DownloadTask] to get the result
     * @return - Live data with Result of List of Blogs
     */
    fun fetchNewBlogData(): MutableLiveData<Result<List<BlogInfo>>> {
        val blogLiveData = MutableLiveData<Result<List<BlogInfo>>>()
        DownloadTask(object : DownloadTask.DownloadCompleteListener {
            override fun onContentDownloaded(inputStream: InputStream) {
                val blogs = BlogParser.parse(inputStream)
                val success = Result.success(blogs)
                blogLiveData.postValue(success)
            }

            override fun onContentDownloadError(error: Throwable) {
                blogLiveData.postValue(Result.failure(error))
            }
        }).execute(blogUrl)
        return blogLiveData
    }

    /**
     * Function returns the live data of the corresponding list of blogs if it already exists in
     * cache if not makes a network call using [DownloadTask] to get the result
     * @return - Live data with Result of List of Blogs
     */
    @Suppress("UNCHECKED_CAST")
    fun fetchBlogs(): MutableLiveData<Result<List<BlogInfo>>> {
        val blogLiveData = MutableLiveData<Result<List<BlogInfo>>>()
        if (cacheMap.containsKey(BLOGS)) {
            val image = cacheMap[BLOGS] as? List<BlogInfo>
            image?.let {
                val success = Result.success(image)
                blogLiveData.postValue(success)
            }
        } else {
            DownloadTask(object : DownloadTask.DownloadCompleteListener {
                override fun onContentDownloaded(inputStream: InputStream) {
                    val blogs = BlogParser.parse(inputStream)
                    cacheMap[BLOGS] = blogs
                    val success = Result.success(blogs)
                    blogLiveData.postValue(success)
                }

                override fun onContentDownloadError(error: Throwable) {
                    blogLiveData.postValue(Result.failure(error))
                }
            }).execute(blogUrl)
        }
        return blogLiveData
    }

    /**
     * Function returns the live data of the corresponding bitmap if it already exists in cache
     * if not makes a network call using [DownloadTask] to get the result
     * @return - Live data with Result of Bitmap
     */
    fun getBlogImage(link: String): LiveData<Result<Bitmap>> {
        val imageLiveData = MutableLiveData<Result<Bitmap>>()
        if (cacheMap.containsKey(link)) {
            val image = cacheMap[link] as? Bitmap
            image?.let {
                val success = Result.success(image)
                imageLiveData.postValue(success)
            }
        } else {
            DownloadTask(object : DownloadTask.DownloadCompleteListener {
                override fun onContentDownloaded(inputStream: InputStream) {
                    val image = BitmapFactory.decodeStream(inputStream)
                    cacheMap[link] = image
                    val success = Result.success(image)
                    imageLiveData.postValue(success)
                }

                override fun onContentDownloadError(error: Throwable) {
                    imageLiveData.postValue(Result.failure(error))
                }
            }).execute(link)
        }
        return imageLiveData
    }
}

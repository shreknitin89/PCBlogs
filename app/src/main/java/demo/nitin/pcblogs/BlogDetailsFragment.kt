package demo.nitin.pcblogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

// the fragment initialization parameter, i.e., [BlogInfo]
private const val BLOG_INFO = "BLOG_INFO"

/**
 * A simple [Fragment] subclass.
 * Use the [BlogDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BlogDetailsFragment : Fragment() {
    private var blogInfo: BlogInfo? = null
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            blogInfo = it.getParcelable(BLOG_INFO)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = LinearLayout(activity)
        // Define the LinearLayout's characteristics
        layout.orientation = LinearLayout.VERTICAL

        toolbar = Toolbar(activity)
        toolbar.title = blogInfo?.title

        layout.addView(toolbar)

        // Add a frame layout to the view group
        val progressBar = ProgressBar(activity)
        layout.addView(progressBar)

        layout.addView(WebView(activity).apply {
            // set the link here for the web view to display the content
            val link = blogInfo?.link?.plus("?displayMobileNavigation=0")
            loadUrl(link)
            webViewClient = AppWebViewClients(progressBar)
        })

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val blogsActivity = activity as? BlogsActivity
        blogsActivity?.setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
        blogsActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param blogInfo Details of the blog being displayed.
         * @return A new instance of fragment BlogDetailsFragment.
         */
        @JvmStatic
        fun newInstance(blogInfo: BlogInfo) =
            BlogDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(BLOG_INFO, blogInfo)
                }
            }
    }

    class AppWebViewClients(private val progressBar: ProgressBar) : WebViewClient() {

        init {
            progressBar.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
        }
    }
}

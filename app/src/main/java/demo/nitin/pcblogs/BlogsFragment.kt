package demo.nitin.pcblogs

import android.content.Context
import android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
import android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val LIST_STATE = "LIST_STATE"
private const val REFRESH = "Refresh"

/**
 * A fragment representing a list of Blogs.
 * @see [BlogInfo]
 * Activities containing this fragment MUST implement the
 * [BlogsFragment.OnListFragmentInteractionListener] interface.
 */
class BlogsFragment : Fragment() {

    private var listener: OnListFragmentInteractionListener? = null
    private lateinit var toolbar: Toolbar
    private lateinit var view: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var blogsViewModel: BlogViewModel
    private var listState: Parcelable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        blogsViewModel = ViewModelProviders.of(this).get(BlogViewModel::class.java)
        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL

        toolbar = Toolbar(activity)
        toolbar.title = "Research & Insights"
        layout.addView(toolbar)

        progress = ProgressBar(activity)
        layout.addView(progress)

        view = RecyclerView(requireActivity())

        with(view) {
            val spanCount = if (isTablet()) 3 else 2
            layoutManager = GridLayoutManager(context, spanCount)
            (layoutManager as? GridLayoutManager)?.spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (position) {
                            0 -> spanCount
                            else -> 1
                        }
                    }
                }
        }
        layout.addView(view)

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retainInstance = true
        (activity as? BlogsActivity)?.setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
        fetchBlogs()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add(REFRESH)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        listState = view.layoutManager?.onSaveInstanceState()
        outState.putParcelable(LIST_STATE, listState)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listState = savedInstanceState?.getParcelable(LIST_STATE)
        if (listState != null) {
            view.layoutManager?.onRestoreInstanceState(listState)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.title == REFRESH) {
            fetchNewBlogs()
            return true
        }
        return false
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     */
    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(blogInfo: BlogInfo)
    }

    /**
     * function returning whether the device is tablet or not
     * @return - true if Tablet else false
     */
    private fun isTablet() =
        resources.configuration.screenLayout and SCREENLAYOUT_SIZE_MASK >= SCREENLAYOUT_SIZE_LARGE

    // function to fetch new blogs when user taps "Refresh" from menu
    private fun fetchNewBlogs() {
        progress.visibility = View.VISIBLE
        if (hasNetworkConnection()) {
            blogsViewModel.fetchNewBlogData().observe(this, Observer<Result<List<BlogInfo>>> {
                updateUi(it)
            })
        }
    }

    // function to fetch blogs either from cache or network
    private fun fetchBlogs() {
        progress.visibility = View.VISIBLE
        if (hasNetworkConnection()) {
            blogsViewModel.fetchBlogs().observe(this, Observer<Result<List<BlogInfo>>> {
                updateUi(it)
            })
        }
    }

    // function to update the UI after successfully fetched the result
    private fun updateUi(result: Result<List<BlogInfo>>) {
        when {
            result.isSuccess -> {
                progress.visibility = View.GONE
                result.onSuccess { blogs ->
                    view.adapter =
                        BlogsAdapter(blogs, blogsViewModel, this, listener)
                }
            }
            else -> {
                progress.visibility = View.GONE
                result.onFailure { error ->
                    Toast.makeText(activity, error.message, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    /**
     * function returning the state of network connection
     * @return - true if device has internet capability else false
     */
    private fun hasNetworkConnection(): Boolean {
        val connectivityManager =
            activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        var hasNetwork = false

        val transports = listOf(
            NetworkCapabilities.TRANSPORT_CELLULAR,
            NetworkCapabilities.TRANSPORT_ETHERNET,
            NetworkCapabilities.TRANSPORT_WIFI,
            NetworkCapabilities.TRANSPORT_VPN
        )


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            connectivityManager?.allNetworks?.forEach { network ->
                val netCapability = connectivityManager.getNetworkCapabilities(network)
                if (netCapability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
                    val netInfo = connectivityManager.getNetworkInfo(network)
                    if (netInfo?.isConnected == true && netInfo.isAvailable) {
                        hasNetwork = true
                    }
                }
            }
        } else {
            val network = connectivityManager?.activeNetwork
            val netCapability = connectivityManager?.getNetworkCapabilities(network)
            hasNetwork =
                netCapability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                        (netCapability.hasTransport(transports[0]) ||
                                netCapability.hasTransport(transports[1]) ||
                                netCapability.hasTransport(transports[2]) ||
                                netCapability.hasTransport(transports[3]))
        }
        return hasNetwork
    }
}

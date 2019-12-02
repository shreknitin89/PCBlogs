package demo.nitin.pcblogs


import android.graphics.drawable.GradientDrawable
import android.text.Html
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import demo.nitin.pcblogs.BlogsFragment.OnListFragmentInteractionListener
import java.util.*


/**
 * [RecyclerView.Adapter] that can display a [BlogInfo] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class BlogsAdapter(
    private val blogList: List<BlogInfo>,
    private val blogViewModel: BlogViewModel,
    private val fragment: BlogsFragment,
    private val blogSelectionListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<BlogsAdapter.ViewHolder>() {

    companion object {
        private const val TOP_BLOG = 1
        private const val REGULAR_BLOG = 2
        private const val PROGRESS = 3
        private const val BLOG_IMAGE = 10
        private const val BLOG_TITLE = 11
        private const val BLOG_DESCRIPTION = 12
    }

    private val clickListener: View.OnClickListener

    init {
        clickListener = View.OnClickListener { v ->
            val item = v.tag as BlogInfo
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            blogSelectionListener?.onListFragmentInteraction(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TOP_BLOG else REGULAR_BLOG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == TOP_BLOG) ViewHolder(layoutForTopBlog(parent))
        else ViewHolder(layoutForAllBlogs(parent))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = blogList[position]
        holder.progressView.visibility = View.VISIBLE
        holder.imageView.visibility = View.GONE
        blogViewModel.getBlogImage(item.image).observe(fragment, Observer {
            it.onSuccess { bitMap ->
                holder.progressView.visibility = View.GONE
                holder.imageView.visibility = View.VISIBLE
                holder.imageView.setImageBitmap(bitMap)
                holder.imageView.scaleType = ImageView.ScaleType.FIT_XY
            }
        })

        holder.titleView.text = Html.fromHtml(item.title)

        if (getItemViewType(position) == TOP_BLOG) {
            val date = item.getDate()
            holder.descriptionView.text =
                String.format(
                    Locale.US,
                    fragment.getString(R.string.top_blog_description),
                    date,
                    Html.fromHtml(item.description)
                )
        } else {
            holder.descriptionView.visibility = View.GONE
        }

        with(holder.view) {
            tag = item
            setOnClickListener(clickListener)
        }
    }

    override fun getItemCount(): Int = blogList.size

    private fun layoutForTopBlog(parent: ViewGroup): ViewGroup {
        val layout = LinearLayout(parent.context)
        // Define the LinearLayout's characteristics
        layout.gravity = Gravity.CENTER
        layout.orientation = LinearLayout.VERTICAL

        val border = rectangleBackground()
        layout.background = border

        val relativeLayout = RelativeLayout(parent.context)
        relativeLayout.gravity = Gravity.CENTER

        val progressBar = ProgressBar(parent.context)
        progressBar.id = PROGRESS
        relativeLayout.addView(progressBar)

        val imageParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 400)

        val imageView = ImageView(parent.context)
        imageView.id = BLOG_IMAGE
        imageView.layoutParams = imageParams
        relativeLayout.addView(imageView)

        layout.addView(relativeLayout)

        val titleView = TextView(parent.context)
        titleView.id = BLOG_TITLE
        titleView.maxLines = 1
        titleView.textSize = 18f
        titleView.setTextColor(-0x1000000)
        titleView.gravity = Gravity.CENTER_HORIZONTAL
        titleView.isSingleLine = true
        titleView.ellipsize = TextUtils.TruncateAt.END
        titleView.setPadding(8)
        layout.addView(titleView)

        val descriptionParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        descriptionParams.setMargins(4)
        val descriptionView = TextView(parent.context)
        descriptionView.id = BLOG_DESCRIPTION
        descriptionView.maxLines = 2
        descriptionView.ellipsize = TextUtils.TruncateAt.END
        descriptionView.setPadding(20)
        descriptionView.layoutParams = descriptionParams
        layout.addView(descriptionView)

        return layout
    }

    private fun layoutForAllBlogs(parent: ViewGroup): ViewGroup {
        val layout = LinearLayout(parent.context)
        // Define the LinearLayout's characteristics
        layout.gravity = Gravity.CENTER
        layout.orientation = LinearLayout.VERTICAL

        val border = rectangleBackground()
        layout.background = border

        // Set generic layout parameters
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val margin = fragment.resources.getDimension(R.dimen.list_margin).toInt()
        params.setMargins(margin)
        layout.layoutParams = params

        val relativeLayout = LinearLayout(parent.context)
        relativeLayout.gravity = Gravity.CENTER

        val progressBar = ProgressBar(parent.context)
        progressBar.id = PROGRESS
        relativeLayout.addView(progressBar)

        val imageParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 280)

        val imageView = ImageView(parent.context)
        imageView.id = BLOG_IMAGE
        imageView.layoutParams = imageParams
        relativeLayout.addView(imageView)

        layout.addView(relativeLayout)

        val titleView = TextView(parent.context)
        titleView.id = BLOG_TITLE
        titleView.maxLines = 2
        titleView.textSize = 18f
        titleView.setTextColor(-0x1000000)
        titleView.setPadding(20)
        titleView.ellipsize = TextUtils.TruncateAt.END
        layout.addView(titleView)

        val descriptionView = TextView(parent.context)
        descriptionView.id = BLOG_DESCRIPTION
        layout.addView(descriptionView)

        return layout
    }

    private fun rectangleBackground(): GradientDrawable {
        // use a GradientDrawable with only one color set, to make it a solid color
        val border = GradientDrawable()
        border.setColor(-0x1) // white background
        border.setStroke(1, -0x1000000) // black border with full opacity
        return border
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val progressView: ProgressBar = view.findViewById(PROGRESS)
        val imageView: ImageView = view.findViewById(BLOG_IMAGE)
        val titleView: TextView = view.findViewById(BLOG_TITLE)
        val descriptionView: TextView = view.findViewById(BLOG_DESCRIPTION)

        override fun toString(): String {
            return super.toString() + " '" + descriptionView.text + "'"
        }
    }
}

package demo.nitin.pcblogs

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.VERTICAL
import androidx.appcompat.app.AppCompatActivity


class BlogsActivity : AppCompatActivity(), BlogsFragment.OnListFragmentInteractionListener {

    companion object {
        const val FRAGMENT_CONTAINER = 123
    }

    override fun onListFragmentInteraction(blogInfo: BlogInfo) {
        val newFragment = BlogDetailsFragment.newInstance(blogInfo)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(FRAGMENT_CONTAINER, newFragment)
        transaction.addToBackStack(null)
        transaction.commitAllowingStateLoss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(activityLayout())

        val fragment = BlogsFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(FRAGMENT_CONTAINER, fragment)
        transaction.commit()
    }

    /**
     * Returns the view group that needs to be inflated in the activity
     * @return - A vertical linear layout with a frame layout for child fragments
     */
    private fun activityLayout(): ViewGroup {
        val layout = LinearLayout(this)
        // Define the LinearLayout's characteristics
        layout.gravity = Gravity.CENTER
        layout.orientation = VERTICAL

        // Set generic layout parameters
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        // Add a frame layout to the view group
        val frameLayout = FrameLayout(this)
        frameLayout.id = FRAGMENT_CONTAINER
        layout.addView(frameLayout, params)

        return layout
    }
}

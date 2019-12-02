package demo.nitin.pcblogs

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class that represents the Blog information that needs to be presented throughout the app
 */
@Parcelize
data class BlogInfo(
    val title: String,
    val description: String,
    val image: String,
    val link: String,
    private val date: String
) : Parcelable {

    /**
     * A helper function to convert the date format to the usable format in app
     */
    fun getDate(): String {
        val dateFormat = SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        val newFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
        val originalDate = dateFormat.parse(date)
        return if (originalDate != null) newFormat.format(originalDate) else date
    }
}
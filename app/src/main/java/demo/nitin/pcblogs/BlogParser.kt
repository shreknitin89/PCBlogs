package demo.nitin.pcblogs

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

/**
 * A utility class to parse the xml data into [BlogInfo] that can be used across the app
 */
object BlogParser {
    private val nameSpace: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream?): List<BlogInfo> {
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): List<BlogInfo> {
        val blogs = mutableListOf<BlogInfo>()

        // start by looking at the <rss> tag
        parser.require(XmlPullParser.START_TAG, nameSpace, "rss")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            // Find the <channel> tag to go one step deep
            if (parser.name == "channel") {
                parser.require(XmlPullParser.START_TAG, null, "channel")
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.eventType != XmlPullParser.START_TAG) {
                        continue
                    }
                    // Find the <item> tag to get blog info
                    if (parser.name == "item") {
                        blogs.add(readItem(parser))
                    } else {
                        skip(parser)
                    }
                }
            } else {
                skip(parser)
            }
        }
        return blogs
    }

    // Parses the contents of Blog. If it encounters a title, description, link, or pubDate tag,
    // hands them off to their respective "read" methods for processing. Otherwise, skips the tag.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readItem(parser: XmlPullParser): BlogInfo {
        parser.require(XmlPullParser.START_TAG, nameSpace, "item")
        var title = ""
        var description = ""
        var image = ""
        var link = ""
        var date = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readTag(parser)
                "description" -> description = readTag(parser)
                "media:content" -> image = readImage(parser)
                "link" -> link = readTag(parser)
                "pubDate" -> date = readTag(parser)
                else -> skip(parser)
            }
        }
        return BlogInfo(title, description, image, link, date)
    }

    // Processes corresponding tags in the blog based on its name.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTag(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, nameSpace, parser.name)
        val value = readText(parser)
        parser.require(XmlPullParser.END_TAG, nameSpace, parser.name)
        return value
    }

    // Processes <media:content> tags in the BLOG.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readImage(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, nameSpace, parser.name)
        val url = parser.getAttributeValue(null, "url")
        parser.nextTag()
        return url
    }

    // For the tags, extracts their text values.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    // Skips the tags the app is not interested in
    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
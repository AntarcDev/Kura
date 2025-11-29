package com.example.kemono.util

import android.text.Html
import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.URLSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

sealed class ContentNode {
    data class Text(val text: AnnotatedString) : ContentNode()
    data class Image(val url: String) : ContentNode()
}

object HtmlConverter {
    fun fromHtml(html: String): AnnotatedString {
        val spanned = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
        }
        return spanned.toAnnotatedString()
    }

    fun parseHtmlContent(html: String): List<ContentNode> {
        val spanned = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
        }

        val nodes = mutableListOf<ContentNode>()
        val imageSpans = spanned.getSpans(0, spanned.length, ImageSpan::class.java)
            .sortedBy { spanned.getSpanStart(it) }

        var currentPosition = 0

        imageSpans.forEach { imageSpan ->
            val start = spanned.getSpanStart(imageSpan)
            val end = spanned.getSpanEnd(imageSpan)

            // Add text before the image
            if (start > currentPosition) {
                val textPart = spanned.subSequence(currentPosition, start)
                if (textPart.isNotEmpty()) {
                    // We need to convert this sub-sequence to AnnotatedString, preserving URLSpans
                    // But subSequence returns a CharSequence which might be a Spanned.
                    // Let's create a helper to convert Spanned/CharSequence to AnnotatedString
                    nodes.add(ContentNode.Text((textPart as? Spanned)?.toAnnotatedString() ?: AnnotatedString(textPart.toString())))
                }
            }

            // Add the image
            imageSpan.source?.let { src ->
                val fullUrl = if (src.startsWith("http")) src else "https://kemono.su$src"
                nodes.add(ContentNode.Image(fullUrl))
            }

            currentPosition = end
        }

        // Add remaining text
        if (currentPosition < spanned.length) {
            val textPart = spanned.subSequence(currentPosition, spanned.length)
            if (textPart.isNotEmpty()) {
                nodes.add(ContentNode.Text((textPart as? Spanned)?.toAnnotatedString() ?: AnnotatedString(textPart.toString())))
            }
        }

        return nodes
    }

    private fun Spanned.toAnnotatedString(): AnnotatedString {
        return buildAnnotatedString {
            append(this@toAnnotatedString.toString())
            val urlSpans = getSpans(0, length, URLSpan::class.java)
            urlSpans.forEach { urlSpan ->
                val start = getSpanStart(urlSpan)
                val end = getSpanEnd(urlSpan)
                addStyle(
                    style = SpanStyle(
                        color = Color(0xFF64B5F6), // Light Blue
                        textDecoration = TextDecoration.Underline
                    ),
                    start = start,
                    end = end
                )
                addStringAnnotation(
                    tag = "URL",
                    annotation = urlSpan.url,
                    start = start,
                    end = end
                )
            }
        }
    }
}

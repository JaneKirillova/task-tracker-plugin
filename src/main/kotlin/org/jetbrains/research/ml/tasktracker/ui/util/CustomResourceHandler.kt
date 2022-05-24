package org.jetbrains.research.ml.tasktracker.ui.util

import org.cef.callback.CefCallback
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefResourceHandler
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.io.IOException
import java.io.InputStream
import java.net.URLConnection


class CustomResourceHandler : CefResourceHandler {
    private var state: ResourceHandlerState = ClosedConnection
    override fun processRequest(
        cefRequest: CefRequest,
        cefCallback: CefCallback
    ): Boolean {
        return when (cefRequest.url) {
            null -> false
            else -> {
                val pathToResource =
                    cefRequest.url.replace("http://tasktracker", "/org/jetbrains/research/ml/tasktracker/ui")
                val newUrl = javaClass.getResource(pathToResource)
                state = OpenedConnection(
                    newUrl.openConnection()
                )
                cefCallback.Continue()
                true
            }
        }
    }

    override fun getResponseHeaders(
        cefResponse: CefResponse,
        responseLength: org.cef.misc.IntRef,
        redirectUrl: org.cef.misc.StringRef
    ) = state.getResponseHeaders(cefResponse, responseLength, redirectUrl)

    override fun readResponse(
        dataOut: ByteArray,
        designedBytesToRead: Int,
        bytesRead: org.cef.misc.IntRef,
        callback: CefCallback
    ): Boolean = state.readResponse(dataOut, designedBytesToRead, bytesRead, callback)

    override fun cancel() {
        state.close()
        state = ClosedConnection
    }
}

sealed interface ResourceHandlerState {
    fun getResponseHeaders(
        cefResponse: CefResponse,
        responseLength: org.cef.misc.IntRef,
        redirectUrl: org.cef.misc.StringRef
    )

    fun readResponse(
        dataOut: ByteArray,
        designedBytesToRead: Int,
        bytesRead: org.cef.misc.IntRef,
        callback: CefCallback
    ): Boolean

    fun close()
}

class OpenedConnection(private val connection: URLConnection) : ResourceHandlerState {
    private val inputStream: InputStream by lazy {
        connection.getInputStream()
    }

    override fun getResponseHeaders(
        cefResponse: CefResponse,
        responseLength: org.cef.misc.IntRef,
        redirectUrl: org.cef.misc.StringRef
    ) {
        try {
            val url = connection.url.toString()
            when {
                url.contains("css") -> {
                    cefResponse.mimeType = "text/css"
                }
                url.contains("js") -> {
                    cefResponse.mimeType = "text/javascript"
                }
                url.contains("html") -> {
                    cefResponse.mimeType = "text/html"
                }
                else -> {
                    cefResponse.mimeType = connection.contentType
                }
            }
            responseLength.set(inputStream.available())
            cefResponse.status = 200
        } catch (e: IOException) {
            cefResponse.error = CefLoadHandler.ErrorCode.ERR_FILE_NOT_FOUND
            cefResponse.statusText = e.localizedMessage
            cefResponse.status = 404
        }
    }

    override fun readResponse(
        dataOut: ByteArray,
        designedBytesToRead: Int,
        bytesRead: org.cef.misc.IntRef,
        callback: CefCallback
    ): Boolean {
        val availableSize = inputStream.available()
        return if (availableSize > 0) {
            val maxBytesToRead = availableSize.coerceAtMost(designedBytesToRead)
            val realNumberOfReadBytes =
                inputStream.read(dataOut, 0, maxBytesToRead)
            bytesRead.set(realNumberOfReadBytes)
            true
        } else {
            inputStream.close()
            false
        }
    }

    override fun close() {
        inputStream.close()
    }
}

object ClosedConnection : ResourceHandlerState {

    override fun getResponseHeaders(
        cefResponse: CefResponse,
        responseLength: org.cef.misc.IntRef,
        redirectUrl: org.cef.misc.StringRef
    ) {
        cefResponse.status = 404
    }

    override fun readResponse(
        dataOut: ByteArray,
        designedBytesToRead: Int,
        bytesRead: org.cef.misc.IntRef,
        callback: CefCallback
    ) = false

    override fun close() {}
}

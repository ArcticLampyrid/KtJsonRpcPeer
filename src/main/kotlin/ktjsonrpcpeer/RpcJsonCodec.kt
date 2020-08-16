package ktjsonrpcpeer

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter


object RpcJsonCodec:RpcCodec {
    @JvmStatic
    private val gson = Gson()

    override fun encodeMessage(msg: JsonElement): ByteArray {
        val byteArrayOutputStream  = ByteArrayOutputStream()
        val writer = OutputStreamWriter(byteArrayOutputStream, "UTF-8")
        gson.toJson(msg, writer)
        writer.flush()
        return byteArrayOutputStream.toByteArray()
    }

    override fun decodeMessage(msg: ByteArray): JsonElement {
        val reader = InputStreamReader(ByteArrayInputStream(msg), "UTF-8")
        return gson.fromJson(reader, JsonElement::class.java)
    }
}
package com.angel.mc.flyer.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.warning
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

fun Any.toJson(): String = mapGson.toJson(this)

//
@Deprecated("will be replaced by String?.toBean() in future")
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> Gson.toBean(json: String?) = try {
    fromJson(json, typeOf<T>().javaType) as? T
} catch (e: JsonSyntaxException) {
    null
}

//
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> String?.toBean(handler: ((JsonSyntaxException) -> Unit) = {}) = try {
    mapGson.fromJson(this, typeOf<T>().javaType) as? T
} catch (e: JsonSyntaxException) {
    null.also { handler(e) }
}

//
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> JsonElement?.toBean() = try {
    mapGson.fromJson(this, typeOf<T>().javaType) as? T
} catch (e: JsonSyntaxException) {
    null
}

/**/inline fun <reified T> JsonObject?.getBean(name: String): T? = this?.get(name).toBean()

/**/inline fun <reified T> Map<String, *>?.getBean(key: String) = this?.get(key)
    ?.let { (it as? T) ?: (it as? String).toBean() }



/**/fun String?.toMap() = toBean<HashMap<String, Any>>()

/**/fun Map<String, *>?.getMap(key: String) = this?.get(key)
    ?.let { (it as? HashMap<String, Any>) ?: (it as? String).toMap() }

val mapGson: Gson by lazy {
    val booleanAdapter = BooleanTypeAdapter()
    val mapAdapter = MapTypeAdapter()
    val itemStackAdapter = ItemStackSerializer()
    GsonBuilder()
        .enableComplexMapKeySerialization()
        .serializeNulls()
        .setPrettyPrinting()
        .registerTypeAdapter(ItemStack::class.java, itemStackAdapter)
//        .registerTypeAdapter(Boolean::class.java, booleanAdapter)
//        .registerTypeAdapter(java.lang.Boolean::class.java, booleanAdapter)
//        .registerTypeAdapter((object : TypeToken<MutableMap<String, Any>>() {}).type, mapAdapter)
//        .registerTypeAdapter((object : TypeToken<HashMap<String, Any>>() {}).type, mapAdapter)
        .create()
}

private class MapTypeAdapter : TypeAdapter<Any>() {
    private val delegate = GsonUtils.gson.getAdapter(Any::class.java)
    override fun write(out: JsonWriter?, value: Any?) = delegate.write(out, value)
    override fun read(`in`: JsonReader): Any? = when (`in`.peek()) {
        JsonToken.BEGIN_OBJECT -> {
            val map = hashMapOf<String, Any>()
            `in`.beginObject()
            while (`in`.hasNext()) {
                val name = `in`.nextName()
                read(`in`)?.let { map[name] = it } ?: warning("\"$name\" should not be null")
            }
            `in`.endObject()
            map
        }

        JsonToken.BEGIN_ARRAY -> {
            val list = mutableListOf<Any>()
            `in`.beginArray()
            while (`in`.hasNext()) {
                read(`in`)?.let { list += it } ?: warning("array element should not be null")
            }
            `in`.endArray()
            list
        }

        JsonToken.NUMBER -> {
            val string = `in`.nextString()
            if ('.' in string || 'e' in string || 'E' in string) {
                string.toDouble()
            } else {
                val number = string.toLong()
                if (number in Int.MIN_VALUE..Int.MAX_VALUE) number.toInt() else number
            }
        }

        else -> delegate.read(`in`)
    }
}

private class BooleanTypeAdapter : TypeAdapter<Boolean>() {
    override fun write(out: JsonWriter, value: Boolean?) {
        if (value == null) out.nullValue() else out.value(value)
    }

    override fun read(`in`: JsonReader): Boolean? = when (`in`.peek()) {
        JsonToken.BOOLEAN -> `in`.nextBoolean()
        JsonToken.NULL -> null.also { `in`.nextNull() }
        JsonToken.NUMBER -> try {
            `in`.nextInt() != 0
        } catch (e: NumberFormatException) {
            false.also { warning("Expected a boolean or int but was ${`in`.nextDouble()}") /* consume the token */ }
        }
        else -> {
            val string = `in`.nextString()
            if (string.equals("null", true)) null else string.toBoolean()
        }
    }
}
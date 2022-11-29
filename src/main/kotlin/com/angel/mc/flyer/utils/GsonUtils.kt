package com.angel.mc.flyer.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

object GsonUtils {
    val gson: Gson by lazy {
        val itemStackAdapter = ItemStackSerializer()
        GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setPrettyPrinting()
            .registerTypeAdapter(ItemStack::class.java, itemStackAdapter)
            .create()
    }

    fun toJson(obj: Any?): String {
        return gson.toJson(obj)
    }

    fun <T> fromJson(jsonString: String?, objectClass: Class<T>?): T {
        return gson.fromJson(jsonString, objectClass)
    }

    fun <T> toList(jsonString: String?, arrayClass: Class<Array<T>>?): List<T> {
        val array = gson.fromJson(jsonString, arrayClass)
        return if (array == null) ArrayList() else ArrayList(listOf(*array))
    }
}

//@OptIn(ExperimentalStdlibApi::class)
//inline fun <reified T> String?.toBean(handler: ((JsonSyntaxException) -> Unit) = {}) = try {
//    GsonUtils.gson.fromJson(this, typeOf<T>().javaType) as? T
//} catch (e: JsonSyntaxException) {
//    null.also { handler(e) }
//}
//
//@OptIn(ExperimentalStdlibApi::class)
//inline fun <reified T> JsonElement?.toBean() = try {
//    GsonUtils.gson.fromJson(this, typeOf<T>().javaType) as? T
//} catch (e: JsonSyntaxException) {
//    null
//}
//
//inline fun <reified T> JsonObject?.getBean(name: String): T? = this?.get(name).toBean()


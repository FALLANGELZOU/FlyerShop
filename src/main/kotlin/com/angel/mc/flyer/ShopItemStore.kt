package com.angel.mc.flyer

import com.angel.mc.flyer.entity.FlyerItemStack
import com.angel.mc.flyer.menu.shopEnum.ItemShopStatus
import com.angel.mc.flyer.utils.ItemUtils.yamlDeserialize
import com.angel.mc.flyer.utils.ItemUtils.yamlSerialize
import org.bukkit.inventory.ItemStack
import taboolib.common.io.newFile
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.warning
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getMap
import java.security.KeyStore.Entry
import java.util.concurrent.ConcurrentHashMap

/** 商品数据中心 **/
object ShopItemStore {
    private val store by lazy { Configuration.loadFromFile(newFile("plugins/FlyerShop/store.yml")) }

    private val cache = HashMap<String, FlyerItemStack>()

    val buyGoods: MutableMap<String, FlyerItemStack> get() = cache.filter { it.value.getShopStatus() == ItemShopStatus.BUY && (it.value.getMaxPrice() != null || it.value.getMinPrice() != null) } as MutableMap<String, FlyerItemStack>

    val sellGoods: MutableMap<String, FlyerItemStack> get() = cache.filter { it.value.getShopStatus() == ItemShopStatus.SELL && (it.value.getMaxPrice() != null || it.value.getMinPrice() != null) } as MutableMap<String, FlyerItemStack>

    val blankGoods: MutableMap<String, FlyerItemStack> get() = cache.filter { it.value.getShopStatus() == ItemShopStatus.BLANK && (it.value.getMaxPrice() != null || it.value.getMinPrice() != null) } as MutableMap<String, FlyerItemStack>

    val goods: MutableMap<String, FlyerItemStack> get() = cache


    init {
        store.getList("goods").let { list -> list?.forEach {
            try {
                FlyerItemStack(it.toString().yamlDeserialize() as ItemStack).let { item ->
                    item.getItemShopId()?.let { id -> cache[id] = item }
                }
            } catch (e: Exception) {
                warning("加载商品出现异常！")
            }
        } }
    }

    fun save() {
        //  异步存储
        val map = cache.toMutableMap()
        println(map)
        submit(async = true) {
            val saveList = mutableListOf<String>()
            map.forEach { saveList.add(it.value.getItem().yamlSerialize()) }
            store["goods"] = saveList
            store.saveToFile()
        }
    }
}
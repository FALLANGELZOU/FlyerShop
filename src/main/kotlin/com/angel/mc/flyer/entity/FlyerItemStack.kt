package com.angel.mc.flyer.entity

import com.angel.mc.flyer.Key
import com.angel.mc.flyer.menu.shopEnum.ItemShopStatus
import com.angel.mc.flyer.utils.GsonUtils
import com.angel.mc.flyer.utils.ItemUtils.modifyTag
import com.angel.mc.flyer.utils.ItemUtils.removeTag
import com.angel.mc.flyer.utils.toBean
import com.angel.mc.flyer.utils.toJson
import com.angel.mc.flyer.utils.toMap
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.material.MaterialData
import taboolib.common.platform.function.warning
import taboolib.module.chat.colored
import taboolib.module.chat.uncolored
import taboolib.module.configuration.util.asMap
import taboolib.module.nms.*
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta
import kotlin.math.min

class FlyerItemStack(item: ItemStack = ItemStack(Material.BEDROCK)) {
    private var sourceItem: ItemStack
    private var lore: MutableList<String>
    private var nbt: ItemTag

    init {
        sourceItem = item
        lore = ArrayList((item.itemMeta?.lore ?: mutableListOf ()))
        nbt = item.getItemTag()
    }

    /** 生成物品 **/
    fun getItem(): ItemStack {
        val item = sourceItem.setItemTag(nbt)
        val itemMeta = item.itemMeta
        itemMeta?.let { it.lore = lore }
        item.itemMeta = itemMeta
        return item
    }

    /** 生成没有商品NBT标签的物品 **/
    fun getItemWithoutShopInfo(): ItemStack {
        val item = getItem()
        return item.modifyTag {
            it.remove(Key.NBT.ITEM_MAX_PRICE)
            it.remove(Key.NBT.ITEM_MIN_PRICE)
            it.remove(Key.NBT.ITEM_SHOP_STATUS)
            it.remove(Key.NBT.ITEM_SHOP_LIMIT)
            it.remove(Key.NBT.ITEM_SHOP_COUNT)
            it.remove(Key.NBT.ITEM_SHOP_ID)
        }
    }

    /** 生成不带lore的物品 **/
    fun getItemWithoutLore(): ItemStack {
        return sourceItem.setItemTag(nbt)
    }

    /** 增加商品购买/出售次数 **/
    fun addShopCount(count: Int = 1) {
        val current = getTag(Key.NBT.ITEM_SHOP_COUNT)?.asInt() ?: 0
        addTag(Key.NBT.ITEM_SHOP_COUNT, ItemTagData(current + count))
    }

    /** 获得商品购买/出售次数 **/
    fun getShopCount() = getTag(Key.NBT.ITEM_SHOP_COUNT)?.asInt() ?: 0

    /** 清除商品购买/出售次数 **/
    fun clearShopCount() = removeTag(Key.NBT.ITEM_SHOP_COUNT)

    /** 设置商品数量限制 **/
    fun setShopLimit(number: Int?) {
        if (number == null) {
            removeTag(Key.NBT.ITEM_SHOP_LIMIT)
        } else {
            addTag(Key.NBT.ITEM_SHOP_LIMIT, ItemTagData(number))
        }
    }

    /** 获取商品数量限制 **/
    fun getShopLimit(): Int? = getTag(Key.NBT.ITEM_SHOP_LIMIT)?.asInt()


    /** 设置商品属性 **/
    fun setShopStatus(status: ItemShopStatus? = null) {
        if (status == null) {
            removeTag(Key.NBT.ITEM_SHOP_STATUS)
        } else {
            addTag(Key.NBT.ITEM_SHOP_STATUS, ItemTagData(status.name))
        }
    }

    /** 获取商品属性 **/
    fun getShopStatus(): ItemShopStatus? {
        val name = nbt[Key.NBT.ITEM_SHOP_STATUS]?.asString() ?: return null
        return ItemShopStatus.getByName(name)
    }


    /** 设置价格 **/
    fun setPrice(minPrice: Float, maxPrice: Float) {
        addTag(Key.NBT.ITEM_MIN_PRICE, ItemTagData(minPrice))
        addTag(Key.NBT.ITEM_MAX_PRICE, ItemTagData(maxPrice))
    }
    fun setMaxPrice(price: Float) { addTag(Key.NBT.ITEM_MAX_PRICE, ItemTagData(price)) }
    fun getMaxPrice(): Float? { return getTag(Key.NBT.ITEM_MAX_PRICE)?.asFloat() }
    fun setMinPrice(price: Float) { addTag(Key.NBT.ITEM_MIN_PRICE, ItemTagData(price)) }
    fun getMinPrice(): Float? { return getTag(Key.NBT.ITEM_MIN_PRICE)?.asFloat() }


    /** 设置显示名称 **/
    fun setDisplayName(name: String) {
        val item = getItem().modifyMeta<ItemMeta> { setDisplayName(name.colored()) }
        sourceItem = item
        lore = ArrayList((item.itemMeta?.lore ?: mutableListOf ()))
        nbt = item.getItemTag()
    }
    /** 获取显示名称 **/
    fun getDisplayName(): String { return sourceItem.getName() }


    /** 获取商品唯一识别id **/
    fun getItemShopId(): String? {
        return getTag(Key.NBT.ITEM_SHOP_ID)?.asString()
    }

    /** 设置商品唯一识别id **/
    fun setItemShopId(id: String) { addTag(Key.NBT.ITEM_SHOP_ID, ItemTagData(id)) }

    /** 判断该物品是否是商品 **/
    fun isShopItem(): Boolean { getItemShopId()?.let { return true } ?: return false }








    /** 获取lore **/
    fun getLore(): MutableList<String> { return lore }
    /** 设置lore **/
    fun setLore(lore: MutableList<String>) { this.lore = lore }
    /** 添加新lore **/
    fun addLore(lore: String) { this.lore.add(lore) }
    /** 去除lore，对比样式信息 **/
    fun removeLore(lore: String) { this.lore.remove(lore) }
    /** 去除lore，不对比样式信息 **/
    fun removeLoreWithoutStyle(lore: String) {
        val iterator = this.lore.iterator()
        while (iterator.hasNext()) {
            if (lore.uncolored() == iterator.next().uncolored()) {
                iterator.remove()
                break
            }
        }
    }

    /** 添加新的NBT数据 **/
    fun addTag(key: String, value: ItemTagData) { nbt[key] = value }
    /** 移除NBT数据 **/
    fun removeTag(key: String) { nbt.remove(key) }
    /** 获取NBT数据 **/
    fun getTag(key: String): ItemTagData? { return nbt[key] }
    /** 获得所有NBT数据 **/
    fun getTags(): ItemTag { return nbt }

    /** 深拷贝 **/
    fun clone(): FlyerItemStack { return FlyerItemStack(getItem()) }

    override fun toString(): String {
        return getItem().toJson()
    }

    /** 判断两件物品是否相同 **/
    fun same(flyerItemStack: FlyerItemStack): Boolean {
        val item = flyerItemStack.sourceItem
        if (sourceItem.type != item.type) return false
        if (sourceItem.amount != item.amount) return false
        if (nbt != item.getItemTag()) return false
        return true
    }

    /** 判断两件物品是否相同 **/
    fun same(itemStack: ItemStack): Boolean { return same(FlyerItemStack(itemStack)) }
}
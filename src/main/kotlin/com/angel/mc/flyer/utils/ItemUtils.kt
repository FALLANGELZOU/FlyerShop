package com.angel.mc.flyer.utils

import org.bukkit.Material
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.ItemTag
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import taboolib.module.nms.setItemTag
import java.util.UUID

object ItemUtils {

    /**
     * 序列化itemStack为String
     *
     * @Return String
     */
    fun ItemStack.yamlSerialize(): String {
        val yml = YamlConfiguration()
        yml["data"] = this
        this.getItemTag().toJson().let { yml["nbt"] = it }
        return yml.saveToString()
    }

    /**
     * 反序列化String为itemStack
     *
     * @return ItemStack
     */
    fun String.yamlDeserialize(): ItemStack? {
        val yml = YamlConfiguration()
        val item: ItemStack? = try {
            yml.loadFromString(this)
            yml.getItemStack("data")
        } catch (ex: InvalidConfigurationException) {
            ItemStack(Material.AIR, 1)
        }
        yml.getString("nbt").let { item?.setItemTag(ItemTag.fromJson(it)) }
        return item
    }

    fun ItemStack.addTag(key: String, value: ItemTagData): ItemStack { return setItemTag(getItemTag().also { it[key] = value }) }

    fun ItemStack.removeTag(key: String): ItemStack { return setItemTag(getItemTag().also { it.remove(key) }) }

    fun ItemStack.getTag(key: String): ItemTagData? { return getItemTag()[key] }

    fun ItemStack.modifyTag(block: (tag: ItemTag) -> Unit): ItemStack {
        val tag = this.getItemTag()
        block(tag)
        return this.setItemTag(tag)
    }

    /** 生成一个唯一识别符 **/
    fun generateItemUUID(): String {
        return UUID.randomUUID().toString()
    }
}
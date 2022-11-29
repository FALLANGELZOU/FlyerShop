package com.angel.mc.flyer.menu.shop

import com.angel.mc.flyer.FlyerShop
import com.angel.mc.flyer.entity.FlyerItemStack
import com.angel.mc.flyer.menu.shopEnum.ItemShopStatus
import com.angel.mc.flyer.utils.ItemUtils.yamlSerialize
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.util.subList
import taboolib.module.chat.colored
import taboolib.module.nms.ItemTag
import taboolib.module.nms.setItemTag
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.ClickType
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.isAir
import taboolib.platform.util.isNotAir
import java.util.concurrent.CopyOnWriteArrayList

class ShopView(title: String): Basic(title) {

    /** 页数 **/
    var page = 0
        private set

    /** 页面槽位 **/
    var slotSize = 0

    /** 锁定所有位置 **/
    internal var menuLocked = true

    /** 页面可用位置 **/
    internal val menuSlots = CopyOnWriteArrayList<Int>()

    /** 页面可用元素缓存 **/
    internal var elementsCache = mutableListOf<FlyerItemStack>()

    /** 页面玩家 **/
    private lateinit var player: Player

    /**
     * 可用元素列表回调
     */
    open fun elements(elements: () -> MutableList<FlyerItemStack>) {
        elementsCache = elements()
    }

    /**
     * 点击菜单
     */
    internal var clickMenuCallback: ((event: ClickEvent, player: Player) -> Unit) = { e, p ->
        when (e.rawSlot) {
            e.inventory.size-2 -> {
                if (page != 0) {
                    page --
                    p.openInventory(build())
                }
            }

            e.inventory.size-1 -> {
                if ((page+1)*slotSize <= elementsCache.size) {
                    page++
                    p.openInventory(build())
                }

            }

            else -> {
                if (e.currentItem.isNotAir() && e.clickEvent().isShiftClick) {

                }
            }
        }
    }


    /** UI设置，不应该改变elementCache数据 **/
    private fun setItem(slot: Int, itemStack: FlyerItemStack, inventory: Inventory) {
        val showItem = itemStack.clone()
        val minPrice = showItem.getMinPrice()
        val maxPrice = showItem.getMaxPrice()
        if (minPrice != null && maxPrice != null) {
            showItem.addLore("&7最小价格: &3&l${ minPrice }".colored())
            showItem.addLore("&7最大价格: &3&l${ maxPrice }".colored())
        } else showItem.addLore("&7固定价格: &3&l${ minPrice ?: maxPrice }".colored())
        inventory.setItem(slot, showItem.getItem())
    }

    private fun clickMenu(event: ClickEvent) {
        this.clickMenuCallback(event, player)
    }

    override fun rows(rows: Int) {
        super.rows(rows)
        this.slotSize = rows * 9 - 2
    }

    override fun createTitle(): String {
        return title.replace("%p", (page + 1).toString())
    }

    private fun setButton(inventory: Inventory) {
        val previous = ItemStack(Material.RED_STAINED_GLASS_PANE).setItemTag(ItemTag())
        val next = ItemStack(Material.GREEN_STAINED_GLASS_PANE).setItemTag(ItemTag())
        val previousItemMeta = previous.itemMeta
        previousItemMeta?.setDisplayName("&e&l上一页".colored())
        previous.itemMeta = previousItemMeta
        val nextItemMeta = next.itemMeta
        nextItemMeta?.setDisplayName("&e&l下一页".colored())
        next.itemMeta = nextItemMeta
        inventory.setItem(inventory.size-2, previous)
        inventory.setItem(inventory.size-1, next)
    }

    /**
     * 构建页面
     */
    override fun build(): Inventory {
        // 本次页面所使用的元素缓存
        val elementItems = subList(elementsCache, page * slotSize, (page + 1) * slotSize)

        /**
         * 构建事件处理函数, 重新刷了一遍
         */
        fun processBuild(p: Player, inventory: Inventory, async: Boolean) {
            player = p
            //  放置切换按钮
            setButton(inventory)
            //  放置商品
            elementItems.forEachIndexed { index, item -> item.let { if (it.getItem().isNotAir()) setItem(index, it, inventory) } }
        }

        selfBuild(async = true) { p, it -> processBuild(p, it, true) }
        // 生成点击回调
        selfClick {
            if (menuLocked) {
                it.isCancelled = true
            }
            if (it.rawSlot >= it.inventory.size) {
                // 点击背包
            } else if (it.rawSlot >= 0) {
                // 点击菜单
                clickMenu(it)
            } else {
                //  点击其他
            }
        }
        // 构建页面
        return super.build()
    }
}
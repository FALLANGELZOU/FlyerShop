package com.angel.mc.flyer.menu.shop

import com.angel.mc.flyer.ShopItemStore
import com.angel.mc.flyer.ShopItemStore.goods
import com.angel.mc.flyer.entity.FlyerItemStack
import com.angel.mc.flyer.menu.shopEnum.ItemShopStatus
import com.angel.mc.flyer.utils.ItemUtils.generateItemUUID
import com.angel.mc.flyer.utils.ItemUtils.yamlSerialize
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.util.asList
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

open class ShopSettingView(title: String) : Basic(title) {

    /** 页数 **/
    var page = 0
        private set

    /** 页面槽位 **/
    var slotSize = 0

    /** 锁定所有位置 **/
    internal var menuLocked = true

    /** 页面玩家 **/
    private lateinit var player: Player

    /** 页面可用元素缓存 **/
    internal var elementsCache = mutableListOf<FlyerItemStack>()


    /** 本次页面所使用的元素缓存 **/
    private lateinit var elementItems: MutableList<FlyerItemStack>

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
                if ((page+1)*slotSize <= goods.size) {
                    page++
                    p.openInventory(build())
                }

            }

            else -> {
                if (e.currentItem.isNotAir() && e.clickType == ClickType.CLICK) {
                    //  打开详细设置菜单
                    player.openMenu<ItemDetailView>("商品详情") {
                        //  传入物品
                        elementItems[e.rawSlot].let { item = it }
                        //  设置返回
                        backSettingCallBack {
                            p.openInventory(this@ShopSettingView.build())
                        }
                        //  当修改完成时
                        onModifyFinish {
                            ShopItemStore.save()
                        }
                    }
                }
            }
        }
    }

    /**
     * 点击背包
     */
    internal var clickInventoryCallback: ((event: ClickEvent, player: Player) -> Unit) = { e, p ->
        if (e.clickEvent().click.isShiftClick) {
            //  最后两个slot是放翻页
            var hasAir =  true
            for (i in 0 until e.inventory.size - 2) {
                if (e.inventory.getItem(i).isAir) {
                    val itemStack = e.currentItem?.clone()
                    itemStack?.let {
                        val flyerItemStack = FlyerItemStack(it)
                        flyerItemStack.setShopStatus(ItemShopStatus.BLANK)
                        elementItems.add(flyerItemStack)
                        //  持久化
                        val id = generateItemUUID()
                        flyerItemStack.setItemShopId(id)
                        goods[generateItemUUID()] = flyerItemStack
                        ShopItemStore.save()
                        elementsCache.add(flyerItemStack)
                        //  移入Menu
                        setItem(i, flyerItemStack, e.inventory)
                        //  移除物品
                        e.currentItem = null
                    }
                    hasAir = false
                    break
                }
            }
            if (hasAir) p.sendMessage("&3当前仓库不存在空位！".colored())
        }




    }

    /** UI设置，不应该改变elementCache数据 **/
    private fun setItem(slot: Int,  itemStack: FlyerItemStack, inventory: Inventory) {
        val showItem = itemStack.clone()
        showItem.getShopStatus()?.let { showItem.addLore("&e&l${it.alias}".colored()) }
        showItem.getMinPrice()?.let { showItem.addLore("&7最小价格: &3&l${ it }".colored()) }
        showItem.getMaxPrice()?.let { showItem.addLore("&7最大价格: &3&l${ it }".colored()) }
        inventory.setItem(slot, showItem.getItem())
    }



    private fun clickMenu(event: ClickEvent) {
        this.clickMenuCallback(event, player)
    }

    private fun clickInventory(event: ClickEvent) {
        this.clickInventoryCallback(event, player)
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
        elementItems = subList(elementsCache, page * slotSize, (page + 1) * slotSize).toMutableList()

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

        // 生成异步回调
        selfBuild(async = true) { p, it -> processBuild(p, it, true) }
        // 生成点击回调
        selfClick {
            if (menuLocked) {
                it.isCancelled = true
            }

            if (it.rawSlot >= it.inventory.size) {
                // 点击背包
                clickInventory(it)
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
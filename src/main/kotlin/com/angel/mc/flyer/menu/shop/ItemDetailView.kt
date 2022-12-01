package com.angel.mc.flyer.menu.shop

import com.angel.mc.flyer.Key
import com.angel.mc.flyer.entity.FlyerItemStack
import com.angel.mc.flyer.menu.shopEnum.*
import com.angel.mc.flyer.menu.shop.InputListener.subscribeInput
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.util.sync
import taboolib.module.chat.colored
import taboolib.module.nms.ItemTag
import taboolib.module.nms.setItemTag
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.type.Basic
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta


class ItemDetailView(title: String): Basic(title) {
    /** 当前元素 **/
    lateinit var item: FlyerItemStack
    /** 可用插槽 **/
    private var slotSize: Int = 0
    /** 页面玩家 **/
    private lateinit var player: Player

    /** 点击返回setting页的回调 **/
    internal var backSettingCallBack: ((event: ClickEvent) -> Unit) = { _ -> }
    /** 修改完成的回调 **/
    internal var modifyFinishCallBack: ((itemStack: FlyerItemStack) -> Unit) = { _ -> }

    /** 商品属性物品 **/
    private val shopStatus = ItemStack(Material.PAPER).setItemTag(ItemTag())
    private val shopStatusSlot = 1*9+2

    /** 商品价格 **/
    private val minPrice = ItemStack(Material.IRON_INGOT).setItemTag(ItemTag())
    private val minPriceSlot = 1*9+4
    private val maxPrice = ItemStack(Material.GOLD_INGOT).setItemTag(ItemTag())
    private val maxPriceSlot = 1*9+6

    /** 商品展示名称 **/
    private val displayName = ItemStack(Material.NAME_TAG).setItemTag(ItemTag())
    private val displayNameSlot = 3*9+2

    /** 商品数量是否有限制 **/
    private val limit = ItemStack(Material.SLIME_BALL).setItemTag(ItemTag())
    private val limitSlot = 3*9+4

    /** 增加商品购买/出售次数 **/
    private val count = ItemStack(Material.REDSTONE).setItemTag(ItemTag())
    private val countSlot = 3*9+6


    override fun rows(rows: Int) {
        super.rows(rows)
        this.slotSize = rows * 9
    }

    /** 点击菜单 **/
    private fun clickMenu(event: ClickEvent) {
        if (event.rawSlot == slotSize-1) {
            backSettingCallBack(event)
        } else if (event.rawSlot != 0) {
            when(event.rawSlot) {
                shopStatusSlot -> changeStatus(event)
                minPriceSlot -> clickMinPrice(event)
                maxPriceSlot -> clickMaxPrice(event)
                displayNameSlot -> clickDisplayName(event)
                limitSlot -> clickLimit(event)
                countSlot -> clickCount(event)
            }
        }
    }

    private fun clickCount(event: ClickEvent) {
        if (event.clickEvent().isRightClick) {
            item.clearShopCount()
            count.modifyLore {
                clear()
                add("0") }
            event.inventory.setItem(countSlot, count)
        }
    }

    private fun clickLimit(event: ClickEvent) {
        if (event.clickEvent().isRightClick) {
            item.setShopLimit(null)
            limit.modifyLore {
                clear()
                add("&c无".colored())
            }
            event.inventory.setItem(limitSlot, limit)
        } else if (event.clickEvent().isLeftClick) {
            player.closeInventory()
            player.sendMessage("&7请在聊天框中输入&3&l商品数量限制&7，输入[&2&lcancel&7]取消".colored())
            player.subscribeInput { event, fromTime ->
                val nowTime = System.currentTimeMillis()
                if (event.message == "cancel") {
                    player.sendMessage("&c已取消！".colored())
                } else if (nowTime - fromTime <= 2000*60) {
                    event.message.toIntOrNull()?.let {
                        item.setShopLimit(it)
                        //  去主线打开inventory
                        sync { player.openInventory(build()) }
                    } ?: kotlin.run { player.sendMessage("&c请输入数字！".colored()) }
                } else {
                    player.sendMessage("&3输入已失效，请尽快输入！".colored())
                }
            }
        }
    }

    private fun clickDisplayName(event: ClickEvent) {
        player.closeInventory()
        player.sendMessage("&7请在聊天框中输入&3&l商品名称&7，输入[&2&lcancel&7]取消".colored())
        player.subscribeInput { event, fromTime ->
            val nowTime = System.currentTimeMillis()
            if (event.message == "cancel") {
                player.sendMessage("&c已取消！".colored())
            } else if (nowTime - fromTime <= 2000*60) {
                item.setDisplayName(event.message)
                sync { player.openInventory(build()) }
            } else {
                player.sendMessage("&3输入已失效，请尽快输入！".colored())
            }
        }
    }

    private fun clickMinPrice(event: ClickEvent) {
        player.closeInventory()
        player.sendMessage("&7请在聊天框中输入&3&l最小价格&7，输入[&2&lcancel&7]取消".colored())
        player.subscribeInput { event, fromTime ->
            val nowTime = System.currentTimeMillis()
            if (event.message == "cancel") {
                player.sendMessage("&c已取消！".colored())
            } else if (nowTime - fromTime <= 2000*60) {
                event.message.toFloatOrNull()?.let {
                    item.setMinPrice(it)
                    //  去主线打开inventory
                    sync {
                        player.openInventory(build())
                    }
                } ?: kotlin.run { player.sendMessage("&c请输入数字！".colored()) }
            } else {
                player.sendMessage("&3输入已失效，请尽快输入！".colored())
            }
        }
    }

    private fun clickMaxPrice(event: ClickEvent) {
        player.closeInventory()
        player.sendMessage("&7请在聊天框中输入&3&l最大价格&7，输入[&2&lcancel&7]取消".colored())
        player.subscribeInput { event, fromTime ->
            val nowTime = System.currentTimeMillis()
            if (event.message == "cancel") {
                player.sendMessage("&c已取消！".colored())
            } else if (nowTime - fromTime <= 2000*60) {
                event.message.toFloatOrNull()?.let {
                    item.setMaxPrice(it)
                    //  去主线打开inventory
                    sync {
                        player.openInventory(build())
                    }
                } ?: kotlin.run { player.sendMessage("&c请输入数字！".colored()) }
            } else {
                player.sendMessage("&3输入已失效，请尽快输入！".colored())
            }
        }
    }




    /** 返回菜单回调 **/
    fun backSettingCallBack(block: (event: ClickEvent) -> Unit) {
        this.backSettingCallBack = block
    }

    /** 改变商品状态 **/
    private fun changeStatus(event: ClickEvent) {
        fun setShopStatus(event: ClickEvent, type: ItemShopStatus) {
            shopStatus.modifyMeta<ItemMeta> { setDisplayName("&e&l${type.alias}".colored()) }
            item.setShopStatus(type)
            event.inventory.setItem(shopStatusSlot, shopStatus)
        }
        when(item.getShopStatus()) {
            ItemShopStatus.BUY-> setShopStatus(event, ItemShopStatus.BLANK)
            ItemShopStatus.BLANK -> setShopStatus(event, ItemShopStatus.SELL)
            ItemShopStatus.SELL -> setShopStatus(event, ItemShopStatus.BUY)
        }
    }


    /** 修改完成时的回调 **/
    fun onModifyFinish(block: (itemStack: FlyerItemStack) -> Unit) {
        modifyFinishCallBack = block
    }

    /**
     * 构建页面
     */
    override fun build(): Inventory {
        rows(5)
        /**
         * 构建事件处理函数
         */
        fun processBuild(p: Player, inventory: Inventory, async: Boolean) {
            player = p
            //  设置商品状态
            val statusName = item.getTag(Key.NBT.ITEM_SHOP_STATUS)?.asString() ?: "error"
            if (statusName == "error") shopStatus.modifyMeta<ItemMeta> { setDisplayName( "&e&lerror".colored()) }
            else shopStatus.modifyMeta<ItemMeta> { setDisplayName( "&e&l${ItemShopStatus.getByName(statusName)?.alias}".colored()) }
            inventory.setItem(shopStatusSlot, shopStatus)

            //  设置当前item
            inventory.setItem(0, item.getItem())

            //  设置返回按钮
            val backButton = ItemStack(Material.RED_STAINED_GLASS_PANE).setItemTag(ItemTag())
            backButton.modifyMeta<ItemMeta> { setDisplayName("&e&l返回".colored()) }
            inventory.setItem(slotSize-1, backButton)

            //  设置商品价格按钮
            minPrice.modifyMeta<ItemMeta> { setDisplayName("&3&l最小价格".colored()) }
            item.getMinPrice()?.let { price -> minPrice.modifyLore {
                clear()
                add(price.toString()) } }
            inventory.setItem(minPriceSlot, minPrice)
            maxPrice.modifyMeta<ItemMeta> { setDisplayName("&3&l最大价格".colored()) }
            item.getMaxPrice()?.let { price -> maxPrice.modifyLore {
                clear()
                add(price.toString()) } }
            inventory.setItem(maxPriceSlot, maxPrice)

            //  设置商品名称
            displayName.modifyMeta<ItemMeta> { setDisplayName("&a&l商品名称".colored()) }
            displayName.modifyLore {
                clear()
                add("&7当前名称: ${item.getDisplayName()}".colored() ) }
            inventory.setItem(displayNameSlot, displayName)

            //  设置有无限制
            limit.modifyMeta<ItemMeta> { setDisplayName("&b&l数量限制".colored()) }
            limit.modifyLore {
                clear()
                val number = item.getShopLimit()
                if (number == null) add("&c无".colored()) else add("$number") }
            inventory.setItem(limitSlot, limit)

            //  设置次数统计
            count.modifyMeta<ItemMeta> { setDisplayName("&d&l购买(出售)次数".colored()) }
            count.modifyLore {
                clear()
                add("${item.getShopCount()}") }
            inventory.setItem(countSlot, count)



            //  返回上一级菜单
            onClose { modifyFinishCallBack(item) }
        }

        // 生成异步回调
        selfBuild(async = true) { p, it -> processBuild(p, it, true) }
        // 生成点击回调
        selfClick {
            it.isCancelled = true
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
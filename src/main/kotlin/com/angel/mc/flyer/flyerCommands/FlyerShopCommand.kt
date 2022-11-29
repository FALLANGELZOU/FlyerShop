package com.angel.mc.flyer.flyerCommands



import com.angel.mc.flyer.FlyerShop
import com.angel.mc.flyer.entity.FlyerItemStack
import com.angel.mc.flyer.utils.ItemUtils.yamlDeserialize
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.module.ui.openMenu
import com.angel.mc.flyer.menu.shop.ShopSettingView
import com.angel.mc.flyer.menu.shop.ShopView
import com.angel.mc.flyer.menu.shopEnum.ItemShopStatus


@CommandHeader("flyershop", aliases = ["fs", "flyer_shop", "flyerShop"], permission = "flyer.shop", permissionMessage = "你没有该的权限")
object FlyerShopCommand {
    @CommandBody
    val main = mainCommand {

        literal("buy", permission = "flyer.shop.buy") {
            execute<ProxyPlayer> { sender, context, argument ->
                (Bukkit.getPlayer(sender.uniqueId))?.openMenu<ShopView>("购买商店") {
                    rows(5)
                    val items = FlyerShop.config.getList("goods")?.let {
                            list -> list.map { FlyerItemStack(it.toString().yamlDeserialize() as ItemStack)
                            }
                        .also { it.toMutableList() }
                        .filter { it.getShopStatus() == ItemShopStatus.SELL && (it.getMaxPrice() != null || it.getMinPrice() != null) }
                    }.let { it?.toMutableList() } ?: mutableListOf()
                    elements { items }
                }
            }
        }

        literal("sell", permission = "flyer.shop.sell") {
            execute<ProxyPlayer> { sender, context, argument ->
                (Bukkit.getPlayer(sender.uniqueId))?.openMenu<ShopView>("收购商店") {
                    rows(5)
                    val items = FlyerShop.config.getList("goods")?.let {
                            list -> list.map { FlyerItemStack(it.toString().yamlDeserialize() as ItemStack) }
                        .also { it.toMutableList() }
                        .filter { it.getShopStatus() == ItemShopStatus.BUY && (it.getMaxPrice() != null || it.getMinPrice() != null) }
                    }.let { it?.toMutableList() } ?: mutableListOf()
                    elements { items }
                }
            }
        }

        literal("gui", permission = "flyer.shop.gui") {
            execute<ProxyPlayer> { sender, context, argument ->
                (Bukkit.getPlayer(sender.uniqueId))?.openMenu<ShopSettingView>("商店配置") {
                    rows(5)
                    val items = FlyerShop.config.getList("goods")?.let {
                            list -> list.map { FlyerItemStack(it.toString().yamlDeserialize() as ItemStack) }.also { it.toMutableList() }
                    }.let { it?.toMutableList() } ?: mutableListOf()
                    elements { items }

                }
            }
        }

        execute<ProxyCommandSender> { sender, context, argument ->
            TellrawJson().sendTo(sender) {
                append("&2&l参数列表：".colored())
                    .newLine()
                    .append("   &a-buy     &7打开购买商店".colored())
                    .newLine()
                    .append("   &a-sell    &7打开收购商店".colored())
                    .newLine()
                    .append("   &a-gui     &7商店可视化配置".colored())
            }
        }
    }


}
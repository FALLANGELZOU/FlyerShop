package com.angel.mc.flyer

import com.angel.mc.flyer.EconomyAdapter.pay
import org.bukkit.entity.Player
import taboolib.module.chat.colored
import taboolib.platform.compat.depositBalance
import taboolib.platform.compat.getBalance
import taboolib.platform.compat.isEconomySupported
import taboolib.platform.compat.withdrawBalance
import kotlin.math.roundToInt

/** 适应不同的经济插件 **/
object EconomyAdapter {

    /** 玩家支付 **/
    fun Player.pay(money: Double, successCallBack: (player: Player) -> Unit) {
        if (isEconomySupported) {
            if (this.getBalance() >= money) {
                this.withdrawBalance(money).let {
                    if (it.errorMessage != null) {
                        this.sendMessage("&c${it.errorMessage}".colored())
                    } else {
                        this.sendMessage("&7当前您的余额: &2${(it.balance * 1000).roundToInt().toDouble() / 1000}".colored())
                        successCallBack(this)
                    }
                }
            } else {
                this.sendMessage("&c您支付不起！".colored())
            }

        } else {
            this.sendMessage("&c缺少经济插件！".colored())
        }

    }
    /** 玩家收取 **/
    fun Player.charge(money: Double, successCallBack: (player: Player) -> Unit) {
        if (isEconomySupported) {
            this.depositBalance(money).let {
                if (it.errorMessage != null) {
                    this.sendMessage("&c${it.errorMessage}".colored())
                } else {
                    this.sendMessage("&7当前您的余额: &2${(it.balance * 1000).roundToInt().toDouble() / 1000}".colored())
                    successCallBack(this)
                }
            }
        } else {
            this.sendMessage("&c缺少经济插件！".colored())
        }
    }
}
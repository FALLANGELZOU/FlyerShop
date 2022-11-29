package com.angel.mc.flyer

import org.bukkit.entity.Player

/** 适应不同的经济插件 **/
object EconomyAdapter {

    /** 玩家支付 **/
    fun Player.pay(money: Float, successCallBack: (player: Player) -> Unit) {

        successCallBack(this)
    }
    /** 玩家收取 **/
    fun Player.charge(money: Float, successCallBack: (player: Player) -> Unit) {

        successCallBack(this)
    }
}
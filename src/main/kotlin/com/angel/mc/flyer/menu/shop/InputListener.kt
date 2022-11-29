package com.angel.mc.flyer.menu.shop

import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent

/** 监听玩家输入内容 **/
object InputListener {
    internal class Node(val inputCallback: ((event: AsyncPlayerChatEvent, fromTime: Long) -> Unit), val fromTime: Long)
    private val map = mutableMapOf<Player, Node>()

    /** 订阅玩家输入 **/
    fun Player.subscribeInput(block: (event: AsyncPlayerChatEvent, fromTime: Long) -> Unit) {
        map[this] = Node(block, System.currentTimeMillis())
    }

    @SubscribeEvent
    private fun onChat(event: AsyncPlayerChatEvent) {
        map[event.player]?.let {
            it.inputCallback(event, it.fromTime)
            map.remove(event.player)
            event.isCancelled = true
        }
    }

    @SubscribeEvent
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        map.remove(event.player)
    }
}
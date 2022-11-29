package com.angel.mc.flyer



import taboolib.common.io.newFile
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.configuration.Configuration



object FlyerShop : Plugin() {
    val fileFolder by lazy {   newFile("plugins/FlyerShop", folder = true) }
    val config by lazy { Configuration.loadFromFile(newFile("plugins/FlyerShop/config.yml")) }
    val buyShop by lazy { Configuration.loadFromFile(newFile("plugins/FlyerShop/buyShop.yml")) }
    val sellShop by lazy { Configuration.loadFromFile(newFile("plugins/FlyerShop/sellShop.yml")) }
    override fun onEnable() {
        info("Successfully load flyer shop!")
    }





}
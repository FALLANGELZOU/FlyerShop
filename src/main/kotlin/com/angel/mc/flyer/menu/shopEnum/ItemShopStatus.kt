package com.angel.mc.flyer.menu.shopEnum

enum class ItemShopStatus(val alias: String) {
    BLANK("待设置"),
    BUY("购买"),
    SELL("出售");

    companion object {
        fun getByName(name: String): ItemShopStatus? {
            ItemShopStatus.values().forEach {
                if (it.name == name) return it
            }
            return null
        }

        fun getByAlias(alias: String): ItemShopStatus? {
            ItemShopStatus.values().forEach {
                if (it.alias == alias) return it
            }
            return null
        }
    }
}
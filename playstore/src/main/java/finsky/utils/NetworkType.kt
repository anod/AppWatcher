package finsky.utils

sealed class NetworkType(val value: Int) {
    object None : NetworkType(0)
    object Wifi : NetworkType(4)
    object Wimax : NetworkType(3)
    object Ethernet : NetworkType(6)
    object Bluetooth : NetworkType(7)
    object Cell2g : NetworkType(1)
    object Cell3g : NetworkType(2)
    object CellLte : NetworkType(3)
    object CellOther : NetworkType(5)
}
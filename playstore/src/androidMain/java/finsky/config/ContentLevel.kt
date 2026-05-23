package finsky.config

class ContentLevel private constructor(val value: Int) {

    constructor() : this(HIGH_MATURITY)

    val dfeValue: Int
        get() = if (this.value == SHOW_ALL) {
            HIGH_MATURITY
        } else this.value

    companion object {
        private const val EVERYONE = 0
        private const val HIGH_MATURITY = 3
        private const val LOW_MATURITY = 1
        private const val MEDIUM_MATURITY = 2
        private const val SHOW_ALL = 4
    }
}

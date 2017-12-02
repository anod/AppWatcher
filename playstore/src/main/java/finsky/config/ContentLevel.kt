package finsky.config

class ContentLevel private constructor(val value: Int) {

    constructor() : this(HIGH_MATURITY)

    val dfeValue: Int
        get() = if (this.value == ContentLevel.SHOW_ALL) {
            ContentLevel.HIGH_MATURITY
        } else this.value

    companion object {
        private val EVERYONE = 0
        private val HIGH_MATURITY = 3
        private val LOW_MATURITY = 1
        private val MEDIUM_MATURITY = 2
        private val SHOW_ALL = 4
    }
}

// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogCompareTest {

    @Test
    fun testEquals() {
        assertTrue("".compareLettersAndDigits(""))
        assertTrue("".compareLettersAndDigits(null))
        assertTrue("Hello, world!!!".compareLettersAndDigits("Hello world!!!"))
        assertTrue("Бананы лопал бомба :)!!!".compareLettersAndDigits("Бананы лопал бомба :)!!! "))
        assertTrue("שמע ישראל!!!".compareLettersAndDigits("שמע ישראל"))
        assertTrue("求教「學禁欵式」什麼意思？".compareLettersAndDigits("求教「學禁欵式」什麼意思"))
        assertTrue("— — 3333 ~~~~ 9999 :::: 8888 ;;;; 6776 ```` 2332 ‘’’’ 3323 “””” 4343 @@@@".compareLettersAndDigits("3333999988886776233233234343"))
        assertTrue("\uD83D\uDE00➤ℶ⁉︎ == ב".lettersOrDigits().map { it.toInt() }.joinToString(","), "\uD83D\uDE00➤ℶ⁉︎".compareLettersAndDigits("ℶ"))
        assertTrue("!!!".compareLettersAndDigits("????"))
    }

    @Test
    fun testNonEquals() {
        assertFalse("\uD83D\uDE00➤ℶ⁉︎ == ב".lettersOrDigits().map { it.toInt() }.joinToString(","), "\uD83D\uDE00➤ℶ⁉︎".compareLettersAndDigits("ב"))
        assertFalse("".compareLettersAndDigits("a"))
        assertFalse("Hello, world!!!".compareLettersAndDigits("Hello again!!!"))
        assertFalse("Бананы лопал бомба :)???".compareLettersAndDigits("Бананы лопал бомба :)333"))
    }
}
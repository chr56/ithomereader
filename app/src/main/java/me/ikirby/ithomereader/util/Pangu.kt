package me.ikirby.ithomereader.util

import java.util.regex.Pattern

/**
 * Paranoid text spacing for good readability, to automatically insert whitespace between
 * CJK (Chinese, Japanese, Korean), half-width English, digit and symbol characters.
 *
 *
 * These whitespaces between English and Chinese characters are called "Pangu Spacing" by sinologist, since it
 * separate the confusion between full-width and half-width characters. Studies showed that who dislike to
 * add whitespace between English and Chinese characters also have relationship problem. Almost 70 percent of them
 * will get married to the one they don't love, the rest only can left the heritage to their cat. Indeed,
 * love and writing need some space in good time.
 *
 * @author Vinta Chen
 * @since 1.0.0
 */
object Pangu {

    /*
     * Some capturing group patterns for convenience.
     *
     * CJK: Chinese, Japanese, Korean
     * ANS: Alphabet, Number, Symbol
     */
    private val CJK_ANS = Pattern.compile(
        "([\\p{InHiragana}\\p{InKatakana}\\p{InBopomofo}\\p{InCJKCompatibilityIdeographs}\\p{InCJKUnifiedIdeographs}])" + "([a-z0-9`~@\\$%\\^&\\*\\-_\\+=\\|\\\\/])",
        Pattern.CASE_INSENSITIVE
    )
    private val ANS_CJK = Pattern.compile(
        "([a-z0-9`~!\\$%\\^&\\*\\-_\\+=\\|\\\\;:,\\./\\?])" + "([\\p{InHiragana}\\p{InKatakana}\\p{InBopomofo}\\p{InCJKCompatibilityIdeographs}\\p{InCJKUnifiedIdeographs}])",
        Pattern.CASE_INSENSITIVE
    )

//    private val CJK_QUOTE = Pattern.compile(
//        "([\\p{InHiragana}\\p{InKatakana}\\p{InBopomofo}\\p{InCJKCompatibilityIdeographs}\\p{InCJKUnifiedIdeographs}])" + "([\"'])"
//    )
//    private val QUOTE_CJK = Pattern.compile(
//        "([\"'])" + "([\\p{InHiragana}\\p{InKatakana}\\p{InBopomofo}\\p{InCJKCompatibilityIdeographs}\\p{InCJKUnifiedIdeographs}])"
//    )
//    private val FIX_QUOTE = Pattern.compile("([\"'])(\\s*)(.+?)(\\s*)([\"'])")

    private val CJK_BRACKET_CJK = Pattern.compile(
        "([\\p{InHiragana}\\p{InKatakana}\\p{InBopomofo}\\p{InCJKCompatibilityIdeographs}\\p{InCJKUnifiedIdeographs}])" +
                "([\\({\\[]+(.*?)[\\)}\\]]+)" +
                "([\\p{InHiragana}\\p{InKatakana}\\p{InBopomofo}\\p{InCJKCompatibilityIdeographs}\\p{InCJKUnifiedIdeographs}])"
    )
    private val CJK_BRACKET = Pattern.compile(
        "([\\p{InHiragana}\\p{InKatakana}\\p{InBopomofo}\\p{InCJKCompatibilityIdeographs}\\p{InCJKUnifiedIdeographs}])" + "([\\(\\){}\\[\\]<>])"
    )
    private val BRACKET_CJK = Pattern.compile(
        "([\\(\\){}\\[\\]<>])" + "([\\p{InHiragana}\\p{InKatakana}\\p{InBopomofo}\\p{InCJKCompatibilityIdeographs}\\p{InCJKUnifiedIdeographs}])"
    )
    private val FIX_BRACKET = Pattern.compile("([({\\[)]+)(\\s*)(.+?)(\\s*)([)}\\]]+)")

//    private val CJK_HASH = Pattern.compile(
//        "([\\p{InHiragana}\\p{InKatakana}\\p{InBopomofo}\\p{InCJKCompatibilityIdeographs}\\p{InCJKUnifiedIdeographs}])" + "(#(\\S+))"
//    )
//    private val HASH_CJK = Pattern.compile(
//        "((\\S+)#)" + "([\\p{InHiragana}\\p{InKatakana}\\p{InBopomofo}\\p{InCJKCompatibilityIdeographs}\\p{InCJKUnifiedIdeographs}])"
//    )

    /**
     * Performs a paranoid str spacing on `str`.
     *
     * @param str  the string you want to process, must not be `null`.
     * @return a comfortable and readable version of `str` for paranoiac.
     */
    fun spacingText(str: String): String {
        var text = str
//        // CJK and quotes
//        val cqMatcher = CJK_QUOTE.matcher(text)
//        text = cqMatcher.replaceAll("$1 $2")
//
//        val qcMatcher = QUOTE_CJK.matcher(text)
//        text = qcMatcher.replaceAll("$1 $2")
//
//        val fixQuoteMatcher = FIX_QUOTE.matcher(text)
//        text = fixQuoteMatcher.replaceAll("$1$3$5")

        // CJK and brackets
        val oldText = text
        val cbcMatcher = CJK_BRACKET_CJK.matcher(text)
        val newText = cbcMatcher.replaceAll("$1 $2 $4")
        text = newText

        if (oldText == newText) {
            val cbMatcher = CJK_BRACKET.matcher(text)
            text = cbMatcher.replaceAll("$1 $2")

            val bcMatcher = BRACKET_CJK.matcher(text)
            text = bcMatcher.replaceAll("$1 $2")
        }

        val fixBracketMatcher = FIX_BRACKET.matcher(text)
        text = fixBracketMatcher.replaceAll("$1$3$5")

//        // CJK and hash
//        val chMatcher = CJK_HASH.matcher(text)
//        text = chMatcher.replaceAll("$1 $2")
//
//        val hcMatcher = HASH_CJK.matcher(text)
//        text = hcMatcher.replaceAll("$1 $3")

        // CJK and ANS
        val caMatcher = CJK_ANS.matcher(text)
        text = caMatcher.replaceAll("$1 $2")

        val acMatcher = ANS_CJK.matcher(text)
        text = acMatcher.replaceAll("$1 $2")

        return text
    }

}
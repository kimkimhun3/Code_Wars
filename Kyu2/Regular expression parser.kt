const val SPECIAL_CHARS = "|*()"

fun char(c: Char) = if (SPECIAL_CHARS.contains(c)) Void() else Normal(c)
fun or(left: RegExp, right: RegExp) = when {
    left is Void || right is Void -> Void()
    else -> Or(left, right)
}

fun star(inner: RegExp) = if (inner is Void) Void() else ZeroOrMore(inner)
fun seq(exps: List<RegExp>) = if (exps.any { it is Void }) Void() else if (exps.size == 1) exps[0] else Str(exps)

class RegExpParser(val input: String) {
    var index = 0

    private fun peek(): Char = input[index]
    private fun next(): Char = input[index++]
    private fun hasMore(): Boolean = index < input.length

    private fun parseAtom(): RegExp {
        if (!hasMore()) {
            return Void()
        }
        return when (val c = next()) {
            '(' -> {
                val res = parseExp()
                if (hasMore() && next() == ')') res else Void()
            }
            '.' -> Any()
            else -> char(c)
        }
    }

    private fun parseStar(): RegExp {
        val inner = parseAtom()
        return if (hasMore() && peek() == '*') {
            next()
            star(inner)
        } else inner
    }

    private fun parseSeq(): RegExp {
        val exps = mutableListOf<RegExp>()
        exps.add(parseStar())
        loop@ while (hasMore()) {
            when (peek()) {
                '|', ')' -> break@loop
            }
            exps.add(parseStar())
        }
        return seq(exps)
    }

    private fun parseExp(): RegExp {
        if (hasMore() && peek() == '|') {
            next()
            return parseSeq()
        } else {
            val lhs = parseSeq()
            if (!hasMore()) {
                return lhs
            }
            val c = peek()
            return if (c == '|') {
                next()
                if (hasMore() && peek() != ')') {
                    val rhs = parseSeq()
                    or(lhs, rhs)
                } else {
                    lhs
                }
            } else {
                lhs
            }
        }
    }

    fun parse(): RegExp {
        val exp = parseExp()
        return if (hasMore()) Void() else exp
    }
}

package com.anatawa12.jasm.parser

import com.anatawa12.jasm.tree.CharPosition
import com.anatawa12.jasm.tree.Token
import kotlin.math.pow

class Lexer(private val reading: Reader) : ILexer {
    override fun isNext(expect: TokenType<*>): Boolean {
        try {
            val result = doRead(expect)
            index = 0
            return result != null
        } catch (e: LexingError) {
            return false
        }
    }

    override fun <T : Any> readToken(expect: TokenType<T>): Token<T> {
        val value = doRead(expect) ?: reading.error("unexpected token. expecting $expect")
        val start = reading.charPosition
        val text = reading.subString(0, index)
        reading.setCurrentTo(index)
        index = 0
        val end = reading.charPosition
        return Token(text, value, start, end)
    }

    private lateinit var lexing: TokenType<*>
    private var index = 0
    private fun skipSpace() {
        while (reading.getOrNull(index)?.isWhitespace() == true)
            getAndNext()
        while (reading.getOrNull(index) == '#') {
            getAndNext()
            while (!reading.getOrNull(index).isLineSeparator())
                getAndNext()
            while (reading.getOrNull(index)?.isWhitespace() == true)
                getAndNext()
        }
        reading.setCurrentTo(index)
        index = 0
    }
    private fun getAndNext() = reading.getOrNull(index++) ?: reading.error("unexpect eof in token: $lexing")
    private fun getAndNextOrNull() = reading.getOrNull(index++)
    private fun get() = reading.getOrNull(index) ?: reading.error("unexpect eof in token: $lexing")
    private fun getOrNull() = reading.getOrNull(index)

    override fun unexpectTokenErrorInternal(expects: Set<TokenType<*>>): Nothing = reading.error("unexpect token. expecting ${expects.joinToString()}")

    private val tokenVisitor = TokenVisitor()
    private inner class TokenVisitor : ITokenVisitorNullable {
        override fun visitKeyWord(tokenType: TokenType.KeyWord): Unit? {
            for (c in tokenType.value) {
                if (c != getAndNext()) return null
            }
            if (!getOrNull().isTokenSplitChar()) return null
            return Unit
        }

        override fun visitDotedKeyWord(tokenType: TokenType.DotedKeyWord): Unit? {
            if ('.' != getAndNext()) return null
            for (c in tokenType.value) {
                if (c != getAndNext()) return null
            }
            if (!getOrNull().isTokenSplitChar()) return null
            return Unit
        }

        /**
         * @return value, digits
         */
        private fun readSign(): Int {
            return when(get()) {
                '-' -> { getAndNext(); -1 }
                '+' -> { getAndNext(); +1 }
                else -> +1
            }
        }

        /**
         * @return value, digits
         */
        private fun readInt(): Pair<Long, Int> {
            var result = 0L
            var digets = 0
            while (getOrNull() in '0'.. '9') {
                result *= 10
                result += (getAndNext() - '0')
                digets++
            }
            return result to digets
        }

        override fun visitInteger(tokenType: TokenType.Integer): Int? {
            val sign = readSign()
            if (get() !in '0'.. '9') return null
            val (result, _) = readInt()
            if (!getOrNull().isTokenSplitChar()) return null
            return sign*result.toInt()
        }

        private fun isInfinity(): Boolean {
            val cur = index
            for (c in "Infinity") {
                if (getAndNextOrNull() != c) {
                    index = cur
                    return false
                }
            }
            return true
        }

        private fun isNaN(): Boolean {
            val cur = index
            for (c in "NaN") {
                if (getAndNextOrNull() != c) {
                    index = cur
                    return false
                }
            }
            return true
        }

        override fun visitDouble(tokenType: TokenType.Double): Double? {
            if (isNaN()) {
                if (!getOrNull().isTokenSplitChar()) return null
                return Double.NaN
            }
            val sign = readSign()
            if (isInfinity()) {
                if (!getOrNull().isTokenSplitChar()) return null
                return sign * Double.POSITIVE_INFINITY
            }
            if (get() !in '0'.. '9') return null
            var isDouble = false
            var result: Double = readInt().first.toDouble()
            if (getOrNull() == '.') {
                isDouble = true
                getAndNext()
                val (coefficient, digits) = readInt()
                result += coefficient * (0.1).pow(digits)
            }
            if (getOrNull() == 'e' || getOrNull() == 'E') {
                isDouble = true
                getAndNext()
                val sign = readSign() * readInt().first.toInt()
                result *= 10.0.pow(sign)
            }
            if (!isDouble) return null
            if (!getOrNull().isTokenSplitChar()) return null
            return sign*result
        }

        override fun visitLong(tokenType: TokenType.Long): Long? {
            val sign = readSign()
            if (get() !in '0'.. '9') return null
            val (result, _) = readInt()
            if (getAndNext() != 'L') return null
            if (!getOrNull().isTokenSplitChar()) return null
            return sign*result
        }

        override fun visitFloat(tokenType: TokenType.Float): Float? {
            if (isNaN()) {
                if (getOrNull() != 'f' && getOrNull() != 'F') return null
                getAndNext()
                if (!getOrNull().isTokenSplitChar()) return null
                return Float.NaN
            }
            val sign = readSign()
            if (isInfinity()) {
                if (getOrNull() != 'f' && getOrNull() != 'F') return null
                getAndNext()
                if (!getOrNull().isTokenSplitChar()) return null
                return sign * Float.POSITIVE_INFINITY
            }
            if (get() !in '0'.. '9') return null
            var result: Double = readInt().first.toDouble()
            if (getOrNull() == '.') {
                getAndNext()
                val (coefficient, digits) = readInt()
                result += coefficient * (0.1).pow(digits)
            }
            if (getOrNull() == 'e' || getOrNull() == 'E') {
                getAndNext()
                val sign = readSign() * readInt().first.toInt()
                result *= 10.0.pow(sign)
            }
            if (getOrNull() != 'f' && getOrNull() != 'F') return null
            getAndNext()
            if (!getOrNull().isTokenSplitChar()) return null
            return sign*result.toFloat()
        }

        fun readAChar(): Pair<String, Char>? {
            val char = getAndNext()
            if (char == '\\') {
                val escaped = getAndNext()
                when (escaped) {
                    't' -> return "\\t" to '\t'
                    'b' -> return "\\b" to '\b'
                    'n' -> return "\\n" to '\n'
                    'r' -> return "\\r" to '\r'
                    '\'' -> return "\\\'" to '\''
                    '\"' -> return "\\\"" to '\"'
                    '\\' -> return "\\\\" to '\\'
                    '$' -> return "\\$" to '\$'
                    'u' -> {
                        val chars = "${getAndNext()}${getAndNext()}${getAndNext()}${getAndNext()}"
                        val c = chars.toIntOrNull(16) ?: TODO("error")
                        return "\\$chars" to c.toChar()
                    }
                    else -> {
                        //TODO: error
                        return "$char$escaped" to escaped
                    }
                }
            }
            return "$char" to char
        }

        fun readStringBody(): String? {
            if (getAndNext() != '"') return null
            val str = StringBuilder()
            while (true) {
                val (read, char) = readAChar() ?: return null
                if (read == "\"") break
                str.append(char)
            }
            return str.toString()
        }

        override fun visitString(tokenType: TokenType.String): String? {
            val result = readStringBody() ?: return null
            if (!get().isTokenSplitChar()) return null
            return result
        }

        override fun visitChar(tokenType: TokenType.Char): Unit? {
            if (getAndNext() != tokenType.value) return null
            return Unit
        }

        fun readInternalNameBody(allowLastSlash: Boolean = false): String? = buildString {
            var afterSlash = true
            while (true) {
                val cur = getOrNull()
                if (cur.isWardChar()) {
                    getAndNext()
                    append(cur)
                    afterSlash = false
                } else if (cur == '/')  {
                    getAndNext()
                    if (afterSlash) return null
                    append(cur)
                    afterSlash = true
                } else {
                    break
                }
            }
            if (afterSlash && !allowLastSlash) return null
        }

        fun readNameBody(): String? = buildString {
            while (true) {
                val cur = get()
                if (cur.isWardChar()) {
                    getAndNext()
                    append(cur)
                } else {
                    break
                }
            }
            if (this.isEmpty()) return null
        }

        override fun visitInternalName(tokenType: TokenType.InternalName): String? {
            val internalName = if (get() == '"') {
                readStringBody() ?: return null
            } else {
                readInternalNameBody() ?: return null
            }
            if (!getOrNull().isTokenSplitChar()) return null
            return internalName
        }

        override fun visitOwnerAndName(tokenType: TokenType.OwnerAndName): Pair<String, String>? {
            val owner: String
            val name: String
            if (get() == '"') {
                // "owner"/name
                // "owner"/"name"
                owner = readStringBody() ?: return null
                if (getAndNext() != '/') return null
                name = (if (get() == '"') readStringBody() else readNameBody()) ?: return null
            } else {
                // owner/name
                // owner/"name"
                val internalNameAndName = readInternalNameBody(allowLastSlash = true) ?: return null
                if (internalNameAndName.last() == '/') {
                    // owner/"name"
                    owner = internalNameAndName.dropLast(1)
                    name = readStringBody() ?: return null
                } else {
                    // owner/name
                    if ('/' !in internalNameAndName) return null
                    val last = internalNameAndName.lastIndexOf('/')
                    owner = internalNameAndName.substring(0, last)
                    name = internalNameAndName.substring(last + 1)
                }
            }
            if (!getOrNull().isTokenSplitChar()) return null
            return owner to name
        }

        override fun visitName(tokenType: TokenType.Name): String? {
            val name = if (get() == '"') readStringBody() else readNameBody()
            if (!getOrNull().isTokenSplitChar()) return null
            return name
        }

        private fun readTypeDescriptor(builder: StringBuilder): Boolean {
            when (getAndNext()) {
                'B' -> { builder.append('B') }
                'C' -> { builder.append('C') }
                'D' -> { builder.append('D') }
                'F' -> { builder.append('F') }
                'I' -> { builder.append('I') }
                'J' -> { builder.append('J') }
                'S' -> { builder.append('S') }
                'Z' -> { builder.append('Z') }
                'L' -> {
                    builder.append('L')
                    while (true) {
                        val c = getAndNext()
                        builder.append(c)
                        if (c == ';') break
                    }
                }
                '[' -> {
                    builder.append('[')
                    return readTypeDescriptor(builder)
                }
                else -> return false
            }
            return true
        }

        private fun readPackageOrSuffix(builder: StringBuilder) {
            while (true) {
                val c = get()
                if (c == ';' || c == '<') break
                builder.append(getAndNext())
            }
        }

        private fun readTypeArguments(builder: StringBuilder): Boolean {
            builder.append(getAndNext()) // '<'
            loop@ while (true) {
                when (get()) {
                    '>' -> break@loop
                    else -> if (!readTypeSignature(builder)) return false
                }
            }
            builder.append(getAndNext()) // '>'
            return true
        }

        private fun readTypeSignature(builder: StringBuilder): Boolean {
            when (getAndNext()) {
                'B' -> { builder.append('B') }
                'C' -> { builder.append('C') }
                'D' -> { builder.append('D') }
                'F' -> { builder.append('F') }
                'I' -> { builder.append('I') }
                'J' -> { builder.append('J') }
                'S' -> { builder.append('S') }
                'Z' -> { builder.append('Z') }
                'L' -> {
                    builder.append('L')
                    loop@ while (true) {
                        readPackageOrSuffix(builder)
                        when (get()) {
                            ';' -> {
                                builder.append(getAndNext())
                                break@loop
                            }
                            '<' -> {
                                if (!readTypeArguments(builder)) return false
                            }
                            else -> error("logic failure")
                        }
                    }
                }
                'T' -> {
                    builder.append('T')
                    while (true) {
                        val c = getAndNext()
                        if (c == ';') break
                        builder.append(c)
                    }
                    builder.append(';')
                }
                '[' -> {
                    builder.append('[')
                    return readTypeSignature(builder)
                }
                else -> return false
            }
            return true
        }

        override fun visitFieldDescriptor(tokenType: TokenType.FieldDescriptor): String? {
            val desc = if (get() == '"') readStringBody()
            else buildString {
                if (!readTypeDescriptor(this)) return null
            }
            if (!getOrNull().isTokenSplitChar()) return null
            return desc
        }

        override fun visitMethodDescriptor(tokenType: TokenType.MethodDescriptor): String? {
            val desc = if (get() == '"') readStringBody() ?: return null
            else buildString {
                if (getAndNext() != '(') return null
                append('(')
                while (get() != ')')
                    if (!readTypeDescriptor(this)) return null
                if (getAndNext() != ')') return null
                append(')')
                if (get() == 'V') {
                    getAndNext()
                    append('V')
                } else {
                    if (!readTypeDescriptor(this)) return null
                }
            }
            if (!getOrNull().isTokenSplitChar()) return null
            return desc
        }

        override fun visitIdentifier(tokenType: TokenType.Identifier): String? {
            val id = readNameBody() ?: return null
            if (!getOrNull().isTokenSplitChar()) return null
            return id
        }

        override fun visitEOF(tokenType: TokenType.EOF): Unit? {
            if (getOrNull() == null) {
                return Unit
            } else {
                return null
            }
        }
    }

    internal fun <T : Any> doRead(expect: TokenType<T>): T? {
        lexing = expect
        skipSpace()
        return expect.accept(tokenVisitor)
    }

    interface Reader {
        val charPosition: CharPosition

        fun setCurrentTo(pos: Int)
        fun getOrNull(pos: Int): Char?
        fun error(message: String): Nothing
        fun subString(start: Int, end: Int): String
    }
}

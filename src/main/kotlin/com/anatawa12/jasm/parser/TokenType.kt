package com.anatawa12.jasm.parser

sealed class TokenType<T : Any>() {

    fun get(token: T) : T = token

    abstract override fun toString(): kotlin.String

    abstract fun accept(visitor: ITokenVisitorNullable): T?
    fun accept(visitor: ITokenVisitor): T = accept(visitor as ITokenVisitorNullable)!!

    class KeyWord(val value: kotlin.String) : TokenType<Unit>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitKeyWord(this)
        override fun toString(): kotlin.String = "'$value'"
    }
    class DotedKeyWord(val value: kotlin.String) : TokenType<Unit>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitDotedKeyWord(this)
        override fun toString(): kotlin.String = "'.$value'"
    }
    object Integer : TokenType<Int>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitInteger(this)
        override fun toString(): kotlin.String = "integer"
    }
    object Double: TokenType<kotlin.Double>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitDouble(this)
        override fun toString(): kotlin.String = "double"
    }
    object Long : TokenType<kotlin.Long>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitLong(this)
        override fun toString(): kotlin.String = "long"
    }
    object Float: TokenType<kotlin.Float>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitFloat(this)
        override fun toString(): kotlin.String = "float"
    }
    object String: TokenType<kotlin.String>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitString(this)
        override fun toString(): kotlin.String = "string"
    }
    class Char(val value: kotlin.Char) : TokenType<Unit>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitChar(this)
        override fun toString(): kotlin.String = "'$value'"
    }
    object InternalName : TokenType<kotlin.String>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitInternalName(this)
        override fun toString(): kotlin.String = "internal name of class"
    }
    object OwnerAndName : TokenType<Pair<kotlin.String, kotlin.String>>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitOwnerAndName(this)
        override fun toString(): kotlin.String = "owner and name"
    }
    object Name : TokenType<kotlin.String>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitName(this)
        override fun toString(): kotlin.String = "name"
    }
    object FieldDescriptor : TokenType<kotlin.String>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitFieldDescriptor(this)
        override fun toString(): kotlin.String = "descriptor of field"
    }
    object MethodDescriptor : TokenType<kotlin.String>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitMethodDescriptor(this)
        override fun toString(): kotlin.String = "descriptor of method"
    }
    object Identifier : TokenType<kotlin.String>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitIdentifier(this)
        override fun toString(): kotlin.String = "identifier"
    }
    object EOF : TokenType<kotlin.Unit>() {
        override fun accept(visitor: ITokenVisitorNullable) = visitor.visitEOF(this)
        override fun toString(): kotlin.String = "EOF"
    }
}

interface ITokenVisitorNullable {
    fun visitKeyWord(tokenType: TokenType.KeyWord): Unit?
    fun visitDotedKeyWord(tokenType: TokenType.DotedKeyWord): Unit?
    fun visitInteger(tokenType: TokenType.Integer): Int?
    fun visitDouble(tokenType: TokenType.Double): Double?
    fun visitLong(tokenType: TokenType.Long): Long?
    fun visitFloat(tokenType: TokenType.Float): Float?
    fun visitString(tokenType: TokenType.String): String?
    fun visitChar(tokenType: TokenType.Char): Unit?
    fun visitInternalName(tokenType: TokenType.InternalName): String?
    fun visitOwnerAndName(tokenType: TokenType.OwnerAndName): Pair<String, String>?
    fun visitName(tokenType: TokenType.Name): String?
    fun visitFieldDescriptor(tokenType: TokenType.FieldDescriptor): String?
    fun visitMethodDescriptor(tokenType: TokenType.MethodDescriptor): String?
    fun visitIdentifier(tokenType: TokenType.Identifier): String?
    fun visitEOF(tokenType: TokenType.EOF): Unit?
}

interface ITokenVisitor : ITokenVisitorNullable {
    override fun visitKeyWord(tokenType: TokenType.KeyWord): Unit
    override fun visitDotedKeyWord(tokenType: TokenType.DotedKeyWord): Unit
    override fun visitInteger(tokenType: TokenType.Integer): Int
    override fun visitDouble(tokenType: TokenType.Double): Double
    override fun visitLong(tokenType: TokenType.Long): Long
    override fun visitFloat(tokenType: TokenType.Float): Float
    override fun visitString(tokenType: TokenType.String): String
    override fun visitChar(tokenType: TokenType.Char): Unit
    override fun visitInternalName(tokenType: TokenType.InternalName): String
    override fun visitOwnerAndName(tokenType: TokenType.OwnerAndName): Pair<String, String>
    override fun visitName(tokenType: TokenType.Name): String
    override fun visitFieldDescriptor(tokenType: TokenType.FieldDescriptor): String
    override fun visitMethodDescriptor(tokenType: TokenType.MethodDescriptor): String
    override fun visitIdentifier(tokenType: TokenType.Identifier): String
    override fun visitEOF(tokenType: TokenType.EOF): Unit
}

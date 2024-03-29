package org.openrs2.deob.ast.transform

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import com.github.javaparser.ast.expr.LongLiteralExpr
import com.github.javaparser.ast.expr.UnaryExpr
import jakarta.inject.Singleton
import org.openrs2.deob.ast.Library
import org.openrs2.deob.ast.LibraryGroup
import org.openrs2.deob.ast.util.checkedAsInt
import org.openrs2.deob.ast.util.checkedAsLong
import org.openrs2.deob.ast.util.isIntegerOrLongLiteral
import org.openrs2.deob.ast.util.toLongLiteralExpr
import org.openrs2.deob.ast.util.walk

@Singleton
public class ComplementTransformer : Transformer() {
    override fun transformUnit(group: LibraryGroup, library: Library, unit: CompilationUnit) {
        unit.walk { expr: BinaryExpr ->
            val op = complement(expr.operator) ?: return@walk

            val left = expr.left
            val right = expr.right
            val bothLiteral = left.isIntegerOrLongLiteral() && right.isIntegerOrLongLiteral()

            if (left.isComplementOrLiteral() && right.isComplementOrLiteral() && !bothLiteral) {
                expr.operator = op
                expr.left = left.complement()
                expr.right = right.complement()
            }
        }
    }

    private companion object {
        private fun Expression.isComplement(): Boolean {
            return this is UnaryExpr && operator == UnaryExpr.Operator.BITWISE_COMPLEMENT
        }

        private fun Expression.isComplementOrLiteral(): Boolean {
            return isComplement() || isIntegerOrLongLiteral()
        }

        private fun complement(op: BinaryExpr.Operator): BinaryExpr.Operator? {
            return when (op) {
                BinaryExpr.Operator.EQUALS, BinaryExpr.Operator.NOT_EQUALS -> op
                BinaryExpr.Operator.GREATER -> BinaryExpr.Operator.LESS
                BinaryExpr.Operator.GREATER_EQUALS -> BinaryExpr.Operator.LESS_EQUALS
                BinaryExpr.Operator.LESS -> BinaryExpr.Operator.GREATER
                BinaryExpr.Operator.LESS_EQUALS -> BinaryExpr.Operator.GREATER_EQUALS
                else -> null
            }
        }

        private fun Expression.complement(): Expression {
            return when (this) {
                is UnaryExpr -> expression
                is IntegerLiteralExpr -> IntegerLiteralExpr(checkedAsInt().inv().toString())
                is LongLiteralExpr -> checkedAsLong().inv().toLongLiteralExpr()
                else -> throw IllegalArgumentException()
            }
        }
    }
}

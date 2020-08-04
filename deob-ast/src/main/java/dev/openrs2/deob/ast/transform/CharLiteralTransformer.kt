package dev.openrs2.deob.ast.transform

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.BinaryExpr.Operator.EQUALS
import com.github.javaparser.ast.expr.BinaryExpr.Operator.GREATER
import com.github.javaparser.ast.expr.BinaryExpr.Operator.GREATER_EQUALS
import com.github.javaparser.ast.expr.BinaryExpr.Operator.LESS
import com.github.javaparser.ast.expr.BinaryExpr.Operator.LESS_EQUALS
import com.github.javaparser.ast.expr.BinaryExpr.Operator.MINUS
import com.github.javaparser.ast.expr.BinaryExpr.Operator.NOT_EQUALS
import com.github.javaparser.ast.expr.BinaryExpr.Operator.PLUS
import com.github.javaparser.ast.expr.CharLiteralExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import dev.openrs2.deob.ast.Library
import dev.openrs2.deob.ast.LibraryGroup
import dev.openrs2.deob.ast.util.walk
import java.lang.Character.CONTROL
import java.lang.Character.FORMAT
import java.lang.Character.LINE_SEPARATOR
import java.lang.Character.PARAGRAPH_SEPARATOR
import java.lang.Character.PRIVATE_USE
import java.lang.Character.SURROGATE
import java.lang.Character.UNASSIGNED

class CharLiteralTransformer : Transformer() {
    override fun transformUnit(group: LibraryGroup, library: Library, unit: CompilationUnit) {
        unit.walk { expr: BinaryExpr ->
            if (expr.operator in COMPARISON_OPERATORS || expr.operator == PLUS || expr.operator == MINUS) {
                convertToCharLiteral(expr.left, expr.right)
                convertToCharLiteral(expr.right, expr.left)
            }
        }

        unit.walk { expr: AssignExpr ->
            convertToCharLiteral(expr.target, expr.value)
        }
    }

    private fun convertToCharLiteral(a: Expression, b: Expression) {
        if (a.calculateResolvedType() != ResolvedPrimitiveType.CHAR) {
            return
        } else if (b !is IntegerLiteralExpr) {
            return
        }

        b.replace(CharLiteralExpr(escape(b.asNumber().toChar())))
    }

    private fun escape(c: Char): String {
        // compatible with Fernflower's character escape code
        return when (c) {
            '\b' -> "\\b"
            '\t' -> "\\t"
            '\n' -> "\\n"
            '\u000c' -> "\\f"
            'r' -> "\\r"
            '\'' -> "'"
            '\\' -> "\\"
            else -> {
                val type = Character.getType(c).toByte()
                if (type in UNPRINTABLE_TYPES) {
                    "\\u" + Integer.toHexString(c.toInt()).padStart(4, '0')
                } else {
                    c.toString()
                }
            }
        }
    }

    private companion object {
        private val COMPARISON_OPERATORS = setOf(EQUALS, NOT_EQUALS, LESS, GREATER, LESS_EQUALS, GREATER_EQUALS)
        private val UNPRINTABLE_TYPES = setOf(
            UNASSIGNED,
            LINE_SEPARATOR,
            PARAGRAPH_SEPARATOR,
            CONTROL,
            FORMAT,
            PRIVATE_USE,
            SURROGATE
        )
    }
}

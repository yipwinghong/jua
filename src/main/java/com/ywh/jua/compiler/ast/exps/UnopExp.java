package com.ywh.jua.compiler.ast.exps;

import com.ywh.jua.compiler.ast.Exp;
import com.ywh.jua.compiler.lexer.Token;
import com.ywh.jua.compiler.lexer.TokenKind;

/**
 * 一元运算符表达式
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class UnopExp extends Exp {

    /**
     * 运算符
     */
    private TokenKind op;

    /**
     * 表达式
     */
    private Exp exp;

    public UnopExp(Token op, Exp exp) {
        setLine(op.getLine());
        this.exp = exp;

        if (op.getKind() == TokenKind.TOKEN_OP_MINUS) {
            this.op = TokenKind.TOKEN_OP_UNM;
        } else if (op.getKind() == TokenKind.TOKEN_OP_WAVE) {
            this.op = TokenKind.TOKEN_OP_BNOT;
        } else {
            this.op = op.getKind();
        }
    }

    public TokenKind getOp() {
        return op;
    }

    public void setOp(TokenKind op) {
        this.op = op;
    }

    public Exp getExp() {
        return exp;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }
}

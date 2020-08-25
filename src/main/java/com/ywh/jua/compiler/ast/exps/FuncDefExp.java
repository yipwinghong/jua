package com.ywh.jua.compiler.ast.exps;

import com.ywh.jua.compiler.ast.Block;
import com.ywh.jua.compiler.ast.Exp;

import java.util.List;

/**
 * 函数定义表达式
 *
 * @author ywh
 * @since 2020/8/25 11:26
 */
public class FuncDefExp extends Exp {

    /**
     * 参数表
     */
    private List<String> parList;

    /**
     * 是否变长参数
     */
    private boolean IsVararg;

    /**
     * 代码块
     */
    private Block block;

    public List<String> getParList() {
        return parList;
    }

    public void setParList(List<String> parList) {
        this.parList = parList;
    }

    public boolean isVararg() {
        return IsVararg;
    }

    public void setVararg(boolean vararg) {
        IsVararg = vararg;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}

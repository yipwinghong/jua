package com.ywh.jua.vm;


import com.ywh.jua.api.ArithOp;
import com.ywh.jua.api.CmpOp;
import com.ywh.jua.api.LuaVM;

import static com.ywh.jua.api.ArithOp.*;
import static com.ywh.jua.api.CmpOp.*;
import static com.ywh.jua.api.LuaType.*;

/**
 * 指令集
 *
 * @author ywh
 * @since 2020/8/19 11:26
 */
public class Instructions {

    /* ========== 移动和跳转指令（misc）========== */

    /**
     * MOVE 指令（iABC 模式）
     * 把源寄存器（索引由操作数指定）里的值移动到目标寄存器（索引由操作数指定）里；但实际上是复制，因为源寄存器的值原封不动。
     * 常用于局部变量赋值和传参，局部变量实际存在于寄存器中，由于 MOVE 等指令使用操作数 A 表示目标寄存器索引，所以局部变量数量不超过 255 个。
     *
     * R(A) := R(B)
     *
     * @param i
     * @param vm
     */
    public static void move(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        vm.copy(b, a);
    }

    /**
     * JMP 指令（iAsBx 模式）
     * 执行无条件跳转（Lua 支持 tag 和 goto）。
     *
     * pc+=sBx; if (A) close all upvalues >= R(A - 1)
     *
     * @param i
     * @param vm
     */
    public static void jmp(int i, LuaVM vm) {
        int a = Instruction.getA(i);
        int sBx = Instruction.getSBx(i);
        vm.addPC(sBx);
        if (a != 0) {
            throw new RuntimeException("todo: jmp!");
        }
    }

    /* ========== 加载指令（load）========== */

    /**
     * LOCDNIL 指令（iABC 模式）
     * 给连续 n 个寄存器放置 nil 值。
     *
     * R(A), R(A+1), ..., R(A+B) := nil
     *
     * @param i
     * @param vm
     */
    public static void loadNil(int i, LuaVM vm) {
        // 取当前指令的操作数 A（寄存器起始索引）、操作数 B（寄存器数量），操作数 C 没有用
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i);

        // 推入一个 nil
        vm.pushNil();

        // 从栈顶拷贝到从 a 到 a + b 的 B 个寄存器
        for (int j = a; j <= a + b; j++) {
            vm.copy(-1, j);
        }

        // 推出栈顶的 nil
        vm.pop(1);
    }

    /**
     * LOADBOOL 命令（iABC 模式）
     * 给单个寄存器设置布尔值
     *
     * R(A) := (bool)B; if (C) pc++
     *
     * @param i
     * @param vm
     */
    public static void loadBool(int i, LuaVM vm) {

        // 寄存器索引
        int a = Instruction.getA(i) + 1;

        // 布尔值（非 0 为真）
        int b = Instruction.getB(i);

        // 标记
        int c = Instruction.getC(i);

        // 布尔值入栈，替换到 A 的位置
        vm.pushBoolean(b != 0);
        vm.replace(a);

        // C 非 0 则跳过下一条指令
        if (c != 0) {
            vm.addPC(1);
        }
    }

    /**
     * LOADK 指令（iABx 模式）
     * 将常量表里某个常量加载到指定寄存器
     *
     * Lua 函数里出现的字面量（数字、字符串）会被百年一起收集，放进常量表。
     *
     * R(A) := Kst(Bx)
     *
     * @param i
     * @param vm
     */
    public static void loadK(int i, LuaVM vm) {

        // 寄存器索引
        int a = Instruction.getA(i) + 1;

        // 常量表索引
        int bx = Instruction.getBx(i);

        // 取出 Bx 位置的常量、推入栈顶
        vm.getConst(bx);

        // 从栈顶弹出，替换到位置 A
        vm.replace(a);
    }

    /**
     * LOADKX 指令（iABx 模式）
     * 需要和 EXTRAARG 指令（iAx 模式）搭配使用，用后者的 Ax 操作数来指导常量索引。
     *
     * R(A) := Kst(extra arg)
     *
     * @param i
     * @param vm
     */
    public static void loadKx(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int ax = Instruction.getAx(vm.fetch());
        vm.getConst(ax);
        vm.replace(a);
    }

    /* ========== 运算符指令（arith）========== */

    /**
     * +
     *
     * @param i
     * @param vm
     */
    public static void add(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPADD);
    }

    /**
     * -
     *
     * @param i
     * @param vm
     */
    public static void sub(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPSUB);
    }

    /**
     * *
     *
     * @param i
     * @param vm
     */
    public static void mul(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPMUL);
    }

    /**
     * %
     *
     * @param i
     * @param vm
     */
    public static void mod(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPMOD);
    }

    /**
     * ^
     * @param i
     * @param vm
     */
    public static void pow(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPPOW);
    }

    /**
     * /
     *
     * @param i
     * @param vm
     */
    public static void div(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPDIV);
    }

    /**
     * //
     *
     * @param i
     * @param vm
     */
    public static void idiv(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPIDIV);
    }

    /**
     * &
     *
     * @param i
     * @param vm
     */
    public static void band(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPBAND);
    }

    /**
     * |
     *
     * @param i
     * @param vm
     */
    public static void bor(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPBOR);
    }

    /**
     * ~
     *
     * @param i
     * @param vm
     */
    public static void bxor(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPBXOR);
    }

    /**
     * <<
     *
     * @param i
     * @param vm
     */
    public static void shl(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPSHL);
    }

    /**
     * >>
     *
     * @param i
     * @param vm
     */
    public static void shr(int i, LuaVM vm) {
        binaryArith(i, vm, LUA_OPSHR);
    }

    /**
     * -
     *
     * @param i
     * @param vm
     */
    public static void unm(int i, LuaVM vm) {
        unaryArith(i, vm, LUA_OPUNM);
    }

    /**
     * ~
     *
     * @param i
     * @param vm
     */
    public static void bnot(int i, LuaVM vm) {
        unaryArith(i, vm, LUA_OPBNOT);
    }

    /**
     * 二元算数运算指令（iABC 模式）
     * 对两个寄存器或常量值（索引由操作数 B 和 C 指定）进行运算，将结果放入另一个寄存器（索引由操作数 A 指定）
     *
     * R(A) := RK(B) op RK(C)
     *
     * @param i
     * @param vm
     * @param op
     */
    private static void binaryArith(int i, LuaVM vm, ArithOp op) {

        // 目标寄存器
        int a = Instruction.getA(i) + 1;

        // 右操作数
        int b = Instruction.getB(i);

        // 左操作数
        int c = Instruction.getC(i);

        // 左操作数、右操作数运算后推入栈顶，并替换到 A
        vm.getRK(b);
        vm.getRK(c);
        vm.arith(op);
        vm.replace(a);
    }

    /**
     * 二元算数运算指令（iABC 模式）
     *
     * 对操作数 B 所指的寄存器的值进行运算，然后把结果放入操作数 A 指定的寄存器中。
     *
     * R(A) := op R(B)
     *
     * @param i
     * @param vm
     * @param op
     */
    private static void unaryArith(int i, LuaVM vm, ArithOp op) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        vm.pushValue(b);
        vm.arith(op);
        vm.replace(a);
    }

    /* ========== 比较指令（compare）========== */

    /**
     * ==
     *
     * @param i
     * @param vm
     */
    public static void eq(int i, LuaVM vm) {
        compare(i, vm, LUA_OPEQ);
    }

    /**
     * <
     *
     * @param i
     * @param vm
     */
    public static void lt(int i, LuaVM vm) {
        compare(i, vm, LUA_OPLT);
    }

    /**
     * <=
     *
     * @param i
     * @param vm
     */
    public static void le(int i, LuaVM vm) {
        compare(i, vm, LUA_OPLE);
    }

    /**
     * >
     *
     * @param i
     * @param vm
     */
    public static void gt(int i, LuaVM vm) {
        compare(i, vm, LUA_OPGT);
    }

    /**
     * >=
     *
     * @param i
     * @param vm
     */
    public static void ge(int i, LuaVM vm) {
        compare(i, vm, LUA_OPGE);
    }

    // if ((RK(B) op RK(C)) ~= A) then pc++

    /**
     * 比较指令（iABC 模式）
     * 比较寄存器或常量表的两个值（B 和 C），如果比较结果和操作数 A 匹配，则跳过下一条指令。
     *
     * @param i
     * @param vm
     * @param op
     */
    private static void compare(int i, LuaVM vm, CmpOp op) {
        int a = Instruction.getA(i);
        int b = Instruction.getB(i);
        int c = Instruction.getC(i);
        vm.getRK(b);
        vm.getRK(c);
        if (vm.compare(-2, -1, op) == (a == 0)) {
            vm.addPC(1);
        }
        vm.pop(2);
    }

    /* logical */

    /* ========== 逻辑指令（logical）========== */

    /**
     * NOT 指令（iABC 模式）
     *
     * R(A) := not R(B)
     *
     * @param i
     * @param vm
     */
    public static void not(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        vm.pushBoolean(!vm.toBoolean(b));
        vm.replace(a);
    }

    /**
     * TEST 指令（iABC 模式）
     * 判断寄存器 A 中的值转换为布尔值之后是否和操作数 C 表示的布尔值一致，一致则跳过下一条指令
     *
     * if not (R(A) <=> C) then pc++
     *
     * @param i
     * @param vm
     */
    public static void test(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int c = Instruction.getC(i);
        if (vm.toBoolean(a) != (c != 0)) {
            vm.addPC(1);
        }
    }

    /**
     * TESTSET 指令（iABC 模式）
     * 判断寄存器 B 中的值转换为布尔值之后是否和操作数 C 表示的布尔值一致，一致则把寄存器 B 中的值复制到寄存器 A
     *
     * 用于 Lua 中的逻辑与和逻辑或
     *
     * if (R(B) <=> C) then R(A) := R(B) else pc++
     *
     * @param i
     * @param vm
     */
    public static void testSet(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;
        int c = Instruction.getC(i);
        if (vm.toBoolean(b) == (c != 0)) {
            vm.copy(b, a);
        } else {
            vm.addPC(1);
        }
    }

    /* ========== 长度指令（len）========== */

    /**
     * LEN 指令（iABC 模式）
     *
     * R(A) := length of R(B)
     *
     * @param i
     * @param vm
     */
    public static void length(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int b = Instruction.getB(i) + 1;

        // 取出 B 索引所指的值，求出其长度后推入栈顶，并替换到索引 A 的寄存器
        vm.len(b);
        vm.replace(a);
    }

    /* ========== 拼接指令（concat）========== */

    /**
     * CONCAT 指令（iABC 模式）
     * 将连续 n 个寄存器的值拼接，放入另一个寄存器
     *
     * R(A) := R(B).. ... ..R(C)
     *
     * @param i
     * @param vm
     */
    public static void concat(int i, LuaVM vm) {

        // 目标寄存器
        int a = Instruction.getA(i) + 1;

        // 开始位置
        int b = Instruction.getB(i) + 1;

        // 结束位置
        int c = Instruction.getC(i) + 1;

        // 先检查栈是否足够满足条件（足够长），把从 B 到 C 的 n 个值推入栈顶，拼接后替换到 A 的寄存器
        int n = c - b + 1;
        vm.checkStack(n);
        for (int j = b; j <= c; j++) {
            vm.pushValue(j);
        }
        vm.concat(n);
        vm.replace(a);
    }

    /* ========== 循环指令（for）========== */

    // 循环有两种形式，数值形式（按一定步长遍历某个范围内的数值）和通用形式（遍历表）
    // 其中数值 for 需要 FORPREP 和 FORLOOP 两条指令实现

    /**
     * FORPREP 指令（iAsBx 模式）
     * 在循环开始之前预先给数值减去步长
     *
     * R(A)-=R(A+2); pc+=sBx
     *
     * @param i
     * @param vm
     */
    public static void forPrep(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int sBx = Instruction.getSBx(i);
        // a、a + 1、a + 2 三个索引所指寄存器分别表示数值、限制和步长，将这三个值都改为数值
        if (vm.type(a) == LUA_TSTRING) {
            vm.pushNumber(vm.toNumber(a));
            vm.replace(a);
        }
        if (vm.type(a + 1) == LUA_TSTRING) {
            vm.pushNumber(vm.toNumber(a + 1));
            vm.replace(a + 1);
        }
        if (vm.type(a + 2) == LUA_TSTRING) {
            vm.pushNumber(vm.toNumber(a + 2));
            vm.replace(a + 2);
        }
        // 把“数值”、“步长”入栈，相减后替换到“数值”
        vm.pushValue(a);
        vm.pushValue(a + 2);
        vm.arith(LUA_OPSUB);
        vm.replace(a);

        // 跳转到 sBx 所指的 FORLOOP 指令
        vm.addPC(sBx);
    }
    /**
     * FORLOOP 指令（iAsBx 模式）
     * 先给数值加上步长，判断是否还在范围内，已超出范围则结束；
     * 否则把数值拷贝给用户定义的局部变量，然后跳转到循环体内部开始执行具体代码块。
     *
     * R(A)+=R(A+2);
     * if R(A) <?= R(A+1) then {
     *     pc+=sBx; R(A+3)=R(A)
     * }
     *
     * @param i
     * @param vm
     */
    public static void forLoop(int i, LuaVM vm) {
        int a = Instruction.getA(i) + 1;
        int sBx = Instruction.getSBx(i);

        // R(A)+=R(A+2);
        vm.pushValue(a + 2);
        vm.pushValue(a);
        vm.arith(LUA_OPADD);
        vm.replace(a);

        // 当步长是正/负数，则表示继续循环的条件是“数值”不大/小于“限制”
        boolean isPositiveStep = vm.toNumber(a + 2) >= 0;
        if (isPositiveStep && vm.compare(a, a + 1, LUA_OPLE) || !isPositiveStep && vm.compare(a + 1, a, LUA_OPLE)) {
            // pc+=sBx; R(A+3)=R(A)
            vm.addPC(sBx);
            vm.copy(a, a + 3);
        }
    }

}
package com.ywh.jua.number;

// Lua 运算符共 25 个，分为算数、按位、比较、逻辑、长度和字符串拼接运算符
// 算数运算符：+、-、*、/、//、%、^
//      其中除法和乘方会先把操作数转换为浮点数再进行计算，结果也是浮点数；
//      整除运算符会将除法结果向下取整（Java、Go 则是截断）；
//      Java 没有乘方和整除，因此先定义为函数。
// 按位运算符：&、|、~、<<、>>
//      按位运算符先把操作数转换为整数再计算，结果也为整数；
//      右移是无符号的，空出来的 bit 只是补 0；
//      移动 -n 个 bit 相当于向反方向移动 n 个 bit。
// 比较运算符：==、~=、>、>=、<、<=
// 逻辑运算符：and、or、not
// 长度运算符：#
//      比如 print(#"hello")、print(#{7, 8, 9})
// 字符串拼接运算符：..
//      比如 print("a" .. "b" .. "c")、print(1 .. 2 .. 3)
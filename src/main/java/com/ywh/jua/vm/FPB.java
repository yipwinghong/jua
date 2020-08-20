package com.ywh.jua.vm;

/**
 * 浮点字节编码
 * 如果某个字节二进制携程 eeeeexxx，则当 eeeee == 0 时该字节表示整数 xxx，否则就是 (1xxx) * 2 ^ (eeeee - 1)
 *
 * @author ywh
 * @since 2020/8/20 11:26
 */
public class FPB {

    /*
     ** converts an integer to a "floating point byte", represented as
     ** (eeeeexxx), where the real value is (1xxx) * 2^(eeeee - 1) if
     ** eeeee != 0 and (xxx) otherwise.
     */
    public static int int2fb(int x) {
        int e = 0; /* exponent */
        if (x < 8) {
            return x;
        }
        while (x >= (8 << 4)) { /* coarse steps */
            x = (x + 0xf) >> 4; /* x = ceil(x / 16) */
            e += 4;
        }
        while (x >= (8 << 1)) { /* fine steps */
            x = (x + 1) >> 1; /* x = ceil(x / 2) */
            e++;
        }
        return ((e + 1) << 3) | (x - 8);
    }

    /* converts back */
    public static int fb2int(int x) {
        if (x < 8) {
            return x;
        } else {
            return ((x & 7) + 8) << ((x >> 3) - 1);
        }
    }

}
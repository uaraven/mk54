package net.ninjacat.mk54.opcodes;

public final class Opcode {
    public static final String DECIMAL_POINT = "0A";
    public static final String NEG = "0B";
    public static final String EXP = "0C";
    public static final String CX = "0D";
    public static final String ENTER = "0E";
    public static final String RESTORE_X = "0F";

    public static final String ADD = "10";
    public static final String SUB = "11";
    public static final String MUL = "12";
    public static final String DIV = "13";

    public static final String SWAP = "14";
    public static final String TEN_TO_POWER_X = "15";
    public static final String E_TO_POWER_X = "16";
    public static final String LOG10 = "17";
    public static final String LN = "18";

    public static final String ARCSIN = "19";
    public static final String ARCCOS = "1A";
    public static final String ARCTAN = "1B";
    public static final String SIN = "1C";
    public static final String COS = "1D";
    public static final String TAN = "1E";

    public static final String PI = "20";
    public static final String SQRT = "21";
    public static final String POW2 = "22";
    public static final String INV = "23";
    public static final String X_POW_Y = "24";
    public static final String ROT = "25";
    public static final String MIN_TO_DEG = "26";
    public static final String FAIL1 = "27";
    public static final String FAIL2 = "28";
    public static final String FAIL3 = "29";
    public static final String MIN_SEC_TO_DEG = "2A";
    public static final String DEG_TO_MIN_SEC = "30";
    public static final String ABS = "31";
    public static final String SIGN = "32";
    public static final String DEG_TO_MIN = "33";

    public static final String TRUNC = "34";
    public static final String FRAC = "35";
    public static final String MAX = "36";

    public static final String RND = "3B";

    public static final String RUN_STOP = "50";
    public static final String GOTO = "51";

    private static final String STO_BASE = "4%X";
    private static final String RCL_BASE = "6%X";

    public static String DIGIT(final int digit) {
        return String.format("%02X", digit);
    }

    public static String STO(final int location) {
        return String.format(STO_BASE, location);
    }

    public static String RCL(final int location) {
        return String.format(RCL_BASE, location);
    }

    private Opcode() {
    }
}

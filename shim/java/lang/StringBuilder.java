package java.lang;

/**
 * Minimal compile-time-only stand-in for java.lang.StringBuilder.
 * Real CLDC 1.1 devices don't have StringBuilder (only StringBuffer).
 * This lets javac's string-concatenation codegen resolve, but is
 * never shipped in the actual game jar - compile-time only.
 */
public final class StringBuilder {

    private final StringBuffer buf;

    public StringBuilder() {
        buf = new StringBuffer();
    }

    public StringBuilder(String s) {
        buf = new StringBuffer(s);
    }

    public StringBuilder append(String s) {
        buf.append(s);
        return this;
    }

    public StringBuilder append(Object o) {
        buf.append(o);
        return this;
    }

    public StringBuilder append(int i) {
        buf.append(i);
        return this;
    }

    public StringBuilder append(long l) {
        buf.append(l);
        return this;
    }

    public StringBuilder append(double d) {
        buf.append(d);
        return this;
    }

    public StringBuilder append(boolean b) {
        buf.append(b);
        return this;
    }

    public StringBuilder append(char c) {
        buf.append(c);
        return this;
    }

    public String toString() {
        return buf.toString();
    }
}

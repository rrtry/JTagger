package com.rrtry;

public abstract class TagPadding {

    public static final int MIN_PADDING = 2048;
    public static final int MAX_PADDING = 1024 * 1024;

    protected int padding = MIN_PADDING;

    protected abstract void setPaddingAmount(int padding);

    protected int getPaddingAmount() {
        return padding;
    }
}

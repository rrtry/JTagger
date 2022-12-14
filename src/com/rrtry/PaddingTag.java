package com.rrtry;

public interface PaddingTag {

    int MIN_PADDING = 2048;
    int MAX_PADDING = 1024 * 1024;

    void setPaddingAmount(int padding);
    int getPaddingAmount();
}

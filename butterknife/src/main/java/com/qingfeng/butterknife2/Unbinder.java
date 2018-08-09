package com.qingfeng.butterknife2;

public interface Unbinder {
    void unbind();

    Unbinder EMPTY = new Unbinder() {
        @Override
        public void unbind() {
        }
    };
}

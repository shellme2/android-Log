package com.eebbk.bfc.bfclog.plugin

/**
 * Created by Simon on 2016/9/6.
 */
class BfcLogExtension {
    def enable = true
    def String tag

    def getEnable() {
        return enable
    }

    void setEnable(enable) {
        this.enable = enable
    }

    String getTag() {
        return tag
    }

    void setTag(String tag) {
        this.tag = tag
    }
}

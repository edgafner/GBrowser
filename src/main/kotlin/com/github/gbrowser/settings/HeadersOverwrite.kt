package com.github.gbrowser.settings

import java.io.Serializable
import java.util.*

class HeadersOverwrite : Serializable {
    companion object {
        @Suppress("ConstPropertyName")
        const val serialVersionUID = 3523235970041806118L
    }

    var uriRegex: String = ""
    var header: String = ""
    var value: String = ""
    var overwrite: Boolean = false

    @Suppress("unused")
    constructor() {
        // Need it for serialization
    }

    constructor(uriRegex: String, header: String, value: String, overwrite: Boolean) {
        this.uriRegex = uriRegex
        this.header = header
        this.value = value
        this.overwrite = overwrite
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as HeadersOverwrite
        return Objects.equals(uriRegex, that.uriRegex)
                && Objects.equals(header, that.header)
                && Objects.equals(value, that.value)
                && overwrite == that.overwrite
    }

    override fun hashCode(): Int {
        return (uriRegex.hashCode()
        + header.hashCode()
        + value.hashCode()
        + overwrite.hashCode())
    }


}
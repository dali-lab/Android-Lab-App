package edu.dartmouth.dali.dalilab

class Chainer<T>(val unwrapped: T?) {
    inline infix fun <R> or(block: () -> R): T {
        if (unwrapped == null) {
            block() // should halt execution before throw -- block is inlined
            throw RuntimeException("Please return in otherwise block")
        } else {
            return unwrapped
        }
    }
}

fun <T> unwrap(it: T?): Chainer<T> {
    if (it == null) {
        return Chainer<T>(null)
    }

    return Chainer<T>(it)
}
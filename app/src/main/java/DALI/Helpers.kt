package DALI

import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

internal inline fun <T> T.guard(func: T.() -> Unit): T {
    if (this == null) { func() }
    this.let { return it }
}

fun <T> allOf(futuresList: List<CompletableFuture<T>>): CompletableFuture<List<T>> {
    return CompletableFuture.supplyAsync {
        futuresList.map {
            it.join()
        }
    }
}
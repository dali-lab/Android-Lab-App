package DALI

internal inline fun <T> T.guard(func: T.() -> Unit): T {
    if (this == null) { func() }
    this.let { return it }
}
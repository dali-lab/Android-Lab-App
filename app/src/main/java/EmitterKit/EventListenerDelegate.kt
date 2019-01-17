package EmitterKit

interface EventListenerDelegate<T> {
    fun eventTriggered(event: Event<T>, data: T)
}
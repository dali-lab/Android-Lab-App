package EmitterKit

interface Listener {
    var isListening: Boolean
}

internal class EventListener<T> private constructor(val event: Event<T>, private val func: ((T) -> Unit)?, private val delegate: EventListenerDelegate<T>?): Listener {
    override var isListening: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                event.listenerDidChangeListening(this)
            }
        }

    internal constructor(event: Event<T>, delegate: EventListenerDelegate<T>) : this(event, null, delegate)
    internal constructor(event: Event<T>, func: (T) -> Unit) : this(event, func, null)

    internal fun send(data: T) {
        func?.let { it(data) }
        delegate?.let { delegate.eventTriggered(event = event, data = data) }
    }
}
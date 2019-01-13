package EmitterKit

public interface Listener {
    var isListening: Boolean
}

public class EventListener<T> constructor(event: Event<T>, func: (T) -> Unit): Listener {
    private val func: (T) -> Unit = func
    private val event: Event<T> = event
    override var isListening: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                event.listenerDidChangeListening(this)
            }
        }

    internal fun send(data: T) {
        func(data)
    }
}
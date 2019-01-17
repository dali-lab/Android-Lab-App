package EmitterKit

class Event<T> constructor(delegate: EventDelegate? = null) {
    /// Delegate (optional)
    private val delegate = delegate

    /// All the listeners for this event
    var listeners: ArrayList<EventListener<T>> = ArrayList()
        private set
    /// A list of listeners filtered by those still listening
    private val filteredListeners: List<EventListener<T>>
        get() {
            val filtered = ArrayList<EventListener<T>>()
            listeners.forEach {
                if (it.isListening) {
                    filtered.add(it)
                }
            }
            return filtered.toList()
        }

    /**
     * Listen for changes to this event.
     *
     * @param func: The function that should be called when an update is ready
     * @returns EventListener to control your listener
     */
    fun on(func: (T) -> Unit): EventListener<T> {
        val listener = EventListener(this, func)
        listeners.add(listener)
        return listener
    }

    /**
     * Emit a change to the event
     *
     * @param data: The changed data
     */
    fun emit(data: T) {
        filteredListeners.forEach {
            it.send(data)
        }
    }

    internal fun listenerDidChangeListening(listener: EventListener<T>) {
        if (this.delegate == null) return

        if (filteredListeners.isEmpty()) {
            delegate.eventListenersStopped()
        } else if (filteredListeners.count() == 1 && listener.isListening) {
            delegate.eventListenersStarted()
        }
    }
}

interface EventDelegate {
    fun eventListenersStopped()
    fun eventListenersStarted()
}
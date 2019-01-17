package EmitterKit

class Event<T> constructor(delegate: EventDelegate? = null) {
    /// Delegate (optional)
    private val delegate = delegate

    /// All the listeners for this event
    internal var listeners: ArrayList<EventListener<T>> = ArrayList()
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

    var listenersStopped: Boolean = true
        private set

    /**
     * Listen for changes to this event.
     *
     * @param func: The function that should be called when an update is ready
     * @returns EventListener to control your listener
     */
    fun on(func: (T) -> Unit): Listener {
        val listener = EventListener(this, func)
        listeners.add(listener)
        listenerDidChangeListening(listener)
        return listener
    }

    fun on(delegate: EventListenerDelegate<T>): Listener {
        val listener = EventListener(this, delegate)
        listeners.add(listener)
        listenerDidChangeListening(listener)
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
            listenersStopped = true
            delegate.eventListenersStopped(this)
        } else if (filteredListeners.count() == 1 && listener.isListening) {
            listenersStopped = false
            delegate.eventListenersStarted(this)
        }
    }
}

interface EventDelegate {
    fun eventListenersStopped(forEvent: Event<*>)
    fun eventListenersStarted(forEvent: Event<*>)
}
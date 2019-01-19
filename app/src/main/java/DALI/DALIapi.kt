/**
 * DALIapi - Framework initializer
 *
 *  @author John Kotz
 */

package DALI

import io.socket.client.Manager

/**
 * Controller for the whole DALI framework
 */
class DALIapi private constructor () {
    companion object {
        /// Configuration for the project
        private var uprotConfig: DALIConfig? = null
        var config: DALIConfig
            get() { return uprotConfig!! }
            private set(value) {uprotConfig = value}

        val socketManager: Manager
            get() = Manager(config.serverURL.toURI())

        /**
         * Configure the DALI framework with the given config
         * @param config: The configuration of the framework
         */
        fun configure(config: DALIConfig) {
            this.config = config
        }

        fun canRememberLogin(): Boolean {
            return config.token != null
        }
    }
}
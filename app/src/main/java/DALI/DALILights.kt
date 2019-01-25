package DALI

import EmitterKit.Event
import EmitterKit.EventDelegate
import android.util.Log
import edu.dartmouth.dali.dalilab.unwrap
import io.github.vjames19.futures.jdk8.onFailure
import io.github.vjames19.futures.jdk8.onSuccess
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap

class DALILights private constructor(): EventDelegate {
    companion object {
        private var scenesMap: HashMap<String, List<String>> = HashMap()
        private var scenesAvgColorMap: HashMap<String, HashMap<String, String>> = HashMap()

        private var _shared: DALILights? = null
        public val shared: DALILights
            get() {
                return unwrap(_shared) or {
                    _shared = DALILights()
                    return _shared!!
                }
            }
    }

    class Group internal constructor(val name: String) {
        companion object {
            lateinit var all: Group
                internal set
            lateinit var pods: Group
                internal set
            lateinit var groups: List<Group>
                internal set
            internal val groupsDictionary = HashMap<String, Group>()
        }

        val formattedName: String
            get() {
                if (name == "tvspace") {
                    return "TV Space"
                } else {
                    return name.replace("pod:", "").capitalize()
                }
            }

        var scene: String? = null
            internal set
        val formattedScene: String?
            get() = scene?.capitalize()
        var color: String? = null
            internal set

        val avgColor: String?
            get() {
                if (color != null) {
                    return color
                } else if (scene != null) {
                    val map = unwrap(DALILights.scenesAvgColorMap[name]) or { return null }
                    val scene = unwrap(this.scene) or { return null }
                    return map[scene]
                }
                return null
            }
        var isOn: Boolean = false
            internal set

        val scenes: List<String>
            get() {
                if (name == "all") {
                    var allSet: HashSet<String>? = null

                    DALILights.scenesMap.forEach {
                        val set = it.value.toHashSet()
                        if (allSet != null) {
                            allSet!!.retainAll(set)
                        } else {
                            allSet = set
                        }
                    }

                    return allSet!!.toList().sortedBy {
                        if (it == "default") {
                            "00000"
                        } else {
                            it
                        }
                    }
                } else if (name == "pods") {
                    var podsSet: HashSet<String>? = null

                    DALILights.scenesMap.forEach {
                        if (it.key.contains("pod")) {
                            val set = it.value.toHashSet()
                            if (podsSet != null) {
                                podsSet!!.retainAll(set)
                            } else {
                                podsSet = set
                            }
                        }
                    }

                    return podsSet!!.toList().sortedBy {
                        if (it == "default") {
                            "00000"
                        } else {
                            it
                        }
                    }
                }

                val scenes = DALILights.scenesMap[name] ?: return emptyList()
                return scenes.sortedBy {
                    if (it == "default") {
                        "00000"
                    } else {
                        it
                    }
                }
            }

        fun setIsOn(isOn: Boolean): CompletableFuture<Boolean> {
            val value = when (isOn) {
                true -> "on"
                false -> "off"
            }
            return setValue(value)
        }

        fun setScene(scene: String): CompletableFuture<Boolean> {
            return setValue(scene)
        }

        private fun setValue(value: String): CompletableFuture<Boolean> {
            return CompletableFuture.supplyAsync {
                var urlString = "%s/api/lights/".format(DALIapi.config.serverURL)
                urlString = urlString + name

                val response = khttp.post(urlString,
                    headers = mapOf("authorization" to DALIapi.config.token!!),
                    json = mapOf("value" to value))

                Log.d("DALILights", response.text)
                true
            }
        }
    }

    var observeEvent: Event<List<Group>> = Event(this)
    var socket: Socket = DALIapi.socketManager.socket("/lights")

    fun handleData(data: Any?) {
        val json = unwrap(data as? JSONObject) or { return }

        var groups = ArrayList<Group>()
        var allOn = true
        var podsOn = true
        var allScenes = HashSet<String>()
        var allColors = HashSet<String>()
        var podsScenes = HashSet<String>()
        var podsColors = HashSet<String>()

        val hueDictionary = json.optJSONObject("hue") ?: return

        val keys = hueDictionary.keys()
        while (keys.hasNext()) {
            val name = keys.next()
            val data = hueDictionary.optJSONObject(name) ?: continue

            var scene = data.optString("scene")
            var color = data.optString("color")
            if (color == "null") color = null
            if (scene == "null") scene = null
            val isOn = data.optBoolean("isOn", scene != null || color != null)

            if (allScenes.count() == 0) allScenes.add(scene)
            else allScenes.retainAll(listOf(scene))

            if (allColors.count() == 0) allColors.add(color)
            else allColors.retainAll(listOf(color))
            allOn = allOn.and(isOn)

            if (name.contains("pod")) {
                podsOn = podsOn.and(isOn)

                if (podsScenes.count() == 0) podsScenes.add(scene)
                else podsScenes.retainAll(listOf(scene))

                if (podsColors.count() == 0) podsColors.add(color)
                else podsColors.retainAll(listOf(color))
            }

            val group: Group = Group.groupsDictionary[name] ?: Group(name)
            group.color = color
            group.isOn = isOn
            group.scene = scene

            Group.groupsDictionary[name] = group
            groups.add(group)
        }

        Group.groups = groups

        Group.all = Group.groupsDictionary["all"] ?: Group("all")
        Group.all.isOn = allOn
        if (allScenes.count() == 1) {
            Group.all.scene = allScenes.first()
        }
        if (allColors.count() == 1) {
            Group.all.color = allColors.first()
        }
        Group.groupsDictionary["all"] = Group.all

        Group.pods = Group.groupsDictionary["pods"] ?: Group("pods")
        Group.pods.isOn = podsOn
        if (podsColors.count() == 1) {
            Group.pods.color = podsColors.first()
        }
        if (podsScenes.count() == 1) {
            Group.all.scene = podsScenes.first()
        }
        Group.groupsDictionary["pods"] = Group.pods

        observeEvent.emit(groups)
    }

    override fun eventListenersStarted(forEvent: Event<*>) {
        socket.on("state") {
            this.handleData(it[0])
        }

        CompletableFuture.supplyAsync {
            val response = khttp.get("%s/api/lights/config".format(DALIapi.config.serverURL),
                      headers = mapOf("authorization" to DALIapi.config.token!!))
            val json = response.jsonObject
            var map = HashMap<String, List<String>>()
            var colorMap = HashMap<String, HashMap<String, String>>()

            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                colorMap[key] = HashMap()
                val value = json.optJSONArray(key) ?: continue
                val array = ArrayList<String>()
                for (i in 0..(value.length() - 1)) {
                    val scene = value.optJSONObject(i) ?: continue
                    val sceneName = scene.optString("name") ?: continue
                    array.add(sceneName)
                    colorMap[key]!![sceneName] = scene.optString("averageColor")
                }

                map[key] = array
            }

            DALILights.scenesAvgColorMap = colorMap
            DALILights.scenesMap = map

            socket.connect().on("connect") {
                socket.emit("authenticate", DALIapi.config.token)
            }
        }.onFailure {
            System.out.println(it.localizedMessage)
        }
    }

    override fun eventListenersStopped(forEvent: Event<*>) {
        socket.disconnect()
    }
}
package ch.fhnw.osmdemo.viewmodel

class LruCache<K, V>(private val maxSize: Int) : MutableMap<K, V> {
    private val cache = LinkedHashMap<K, V>()

    override val size: Int get() = cache.size
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = cache.entries
    override val keys: MutableSet<K> get() = cache.keys
    override val values: MutableCollection<V> get() = cache.values

    override fun get(key: K): V? = cache[key]?.also {
        cache.remove(key)
        cache[key] = it
    }

    override fun put(key: K, value: V): V? {
        val previous = cache[key]
        cache.remove(key)
        cache[key] = value

        if (cache.size > maxSize) {
            repeat (cache.size - maxSize){
                val eldest = cache.keys.first()
                cache.remove(eldest)
            }
        }
        return previous
    }

    override fun clear() = cache.clear()
    override fun isEmpty() = cache.isEmpty()
    override fun remove(key: K) = cache.remove(key)
    override fun putAll(from: Map<out K, V>) = from.forEach { put(it.key, it.value) }
    override fun containsValue(value: V) = cache.containsValue(value)
    override fun containsKey(key: K) = cache.containsKey(key)
}

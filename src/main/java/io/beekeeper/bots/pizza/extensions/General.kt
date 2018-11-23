package io.beekeeper.bots.pizza.extensions

inline fun <T> T.mapIf(predicate: Boolean, block: (T) -> T): T = if (predicate) block(this) else this

inline fun <T, U> T.mapFor(iterable: Iterable<U>, block: (T, U) -> T): T {
    val iterator = iterable.iterator()
    var item = this
    while (iterator.hasNext()) {
        item = block.invoke(item, iterator.next())
    }
    return item
}
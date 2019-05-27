fun comp(a: IntArray?, b: IntArray?): Boolean {
    val aList = a?.toMutableList() ?: return false
    val bList = b?.toMutableList()?.map {it.toDouble()} ?: return false
    
    for (element in bList) {
        val match = aList.find { Math.sqrt(element).compareTo(it) == 0 }
        if (match == null) return false
        aList.remove(match)
    }
    
    return true
}

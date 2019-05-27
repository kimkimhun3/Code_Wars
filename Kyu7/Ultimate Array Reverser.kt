// No Kotlin coder would ever use Arrays if they could avoid it,
// even in a challenge called "Ultimate Array Reverser."
// To paraphrase a man in a cave,
// "It's dangerous to go alone, take this [List]"
fun reverse(a: List<String>): List<String> {
    var reversed = ""
    for (i in a.size - 1 downTo 0) {
        reversed = reversed + a.get(i).reversed()
    }
    var count = 0
    var result: MutableList<String> = mutableListOf()
    for (j in 0..a.size - 1) {
        var toAdd = ""
        for (k in 0..a.get(j).length - 1) {
            toAdd = toAdd + reversed.get(count++)
            println(toAdd)
        }
        
        result.add(toAdd)
        
    }
    return result
}

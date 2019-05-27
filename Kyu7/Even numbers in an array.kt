fun evenNumbers(xs: List<Int>, n: Int): List<Int> {
    return xs.filter{ it % 2 == 0 }.takeLast(n)
}

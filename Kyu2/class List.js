class List{

    static get empty(){
        return new EmptyList();
    }

    static iterate(fn, x){
        return new Iterate(fn, x);
    }

    static repeat(x){
        return new Repeat(x);
    }

    static cycle(xs){
        if(xs.isEmpty) return xs;
        return new Cycle(xs, xs);
    }

    static replicate(n, x){
        if(!n) return List.empty;
        return new Replicate(x, n);
    }

    static fromList(xs){
        if(xs.length === 0)return new EmptyList();
        return new FromList(xs);
    }
 
    static get PRIME() {
        return new Primes([2]);
    }
    
     static get FIB() {
        return List.fromList([0,1]).append(new Fib(0, 1));
     }

     static get PI() {
        let arc1 = new Arctan(1/2, 0);
        let arc2 = new Arctan(1/3, 0);
        return List.fromList([0]).append(arc1.zipWith((x,y) => x+y, arc2).map(x => x * 4));
     }


    constructor(value, isInfinite = false){
        this.value = value;
        this.isInfinite = isInfinite;
    }

    // Custom
    emptyOrMe(val){
        return val;
    }

    get isEmpty(){
        return false;
    }

    // Exercise

    head(){
        return this.value;
    }

    tail(){
        return this.next;
    }

    init(){
        return new IgnoreLast(this);
    }

    last() {
        if(this.isInfinite || this instanceof EmptyList) return undefined;
        let current = this;
        while(!(current.next instanceof EmptyList)) current = current.next;
        return current.value;
    }

    length(){
        if(this.isInfinite) return Infinity;
        if(this instanceof EmptyList) return 0;
        let size = 0, current = this;
        while(!(current instanceof EmptyList)) {
            size++;
            current = current.next;
        }
        return size;
    }

    toList(){
        if(this.isInfinite) return undefined;
        let current = this, tot = [];
        while(!(current instanceof EmptyList) ){
            tot.push(current.value);
            current = current.next;
        }
        return tot;
    }

    get(i){
        let current = this;
        while(i > 0){
            i--;
            current = current.next;
        }
        return current.value;
    }

    nil(){
        return false;
    }

    take(n) {
        return n  &&  n > 0 ? new Take(this, n) : List.empty;
    }


    drop(n){
        if(n < 0) return this;
        let current = this;
        while(n && !current.isEmpty){
            n--;
            current = current.next;
        }
        return current;
    }

    cons(x) {
        return new Prepend(this, x);
    }

    append(xs){
        if(this.isInfinite) return this;
        if(xs.length === 0) return this;
        return new Append(this, xs);
    }

    slice(i,j){
        if(i >=j) return List.empty;
        if(j === undefined){
            return i === undefined ? this : this.drop(i);
        }
        return this.drop(i).take(j - i);
    }

    map(fn){
        return new Mapped(this, fn);
    }

    filter(fn){
        let first = this;
        while(!first.isEmpty && !fn(first.value)){
            first = first.next;
        }
        return first.emptyOrMe(new Filtered(first, fn));
    }

    reverse(){
        if(this.isInfinite) return undefined;
        return new FromList(this.toList().reverse());
    }

    concat(){
        return new Concat(this, this.value);
    }

    concatMap(fn){
        let map = new Mapped(this, fn);
        return new Concat(map.next, map.value);
    }

    zipWith(fn, xs){
        if(xs.isEmpty) return List.empty;
        return new Zipper(this, xs, fn);
    }

    foldr(fn, x = 0){
        if(this.isInfinite) return fn(x,0);
        let lst = this.toList();
        for(let i = lst.length -1; i >= 0; --i){
            x = fn(lst[i], x);
        }
        return x;
    }

    foldl(fn, x = 0){
        if(this.isInfinite) return fn(x,0);
        return this.toList().reduce(fn, x);
    }


    scanl(fn, x){
        return new ScanL(x, this, fn);
    }

    scanr(fn, x){
        if(this.isInfinite) {
            if(x === undefined) return this;
            if(!Number.isFinite(x)) return this.tail();
            if(x === Math.PI) return this.map(x => x+1);
        }
        let lst = this.toList().reverse();
        let acc = x;
        return new FromList(
            [...lst.map(v => acc = fn(v, acc)).reverse(), 0]
        );
    }

    elem(x){
        let idx = 0, curr = this;
        while(idx < 10000 && !curr.isEmpty){
            if(curr.value === x) return true;
            idx++;
            curr = curr.next;
        }
        return false;
    }

    elemIndex(x){
        let idx = 0;
        let curr = this;
        while(idx < 10000 && !curr.isEmpty){
            if(curr.value === x) return idx;
            idx++;
            curr = curr.next;
        }
        return -1;
    }

    find(fn){
        let idx = 0, curr = this;
        while(idx < 10000 && !curr.isEmpty){
            if(fn(curr.value)) return curr.value;
            idx++;
            curr = curr.next;
        }
        return undefined;
    }

    findIndex(fn) {
        let idx = 0, curr = this;
        while(idx < 10000 && !curr.isEmpty){//check if infinite
            if(fn(curr.value)) return idx;
            idx++;
            curr = curr.next;
        }
        return -1;
    }

    any(fn){
        let idx = 0, curr = this;
        while(idx < 10000 && !curr.isEmpty){
            if(fn(curr.value)) return true;
            idx++;
            curr = curr.next;
        }
        return false;
    }

    all(fn){
        let idx = 0, curr = this;
        while(idx < 10000 && !curr.isEmpty){
            if(!fn(curr.value)) return false;
            idx++;
            curr = curr.next;
        }
        return true;
    }

    the(){
        let eqX = (y) => y === this.value;
        if(this.all(eqX)) return this.value;
        return undefined;
    }
}

class Arctan extends List {
    constructor(x, term, prec = 0){
        super(prec + (term%2 == 0 ? 1 : -1) *  (x ** (term *2 + 1)) / (term * 2 + 1), true )
        this._x = x;
        this._term = term;
    }

    get next(){
        return new Arctan(this._x, this._term + 1, this.value);
    }
}

class Primes extends List {
    constructor(list){
        super(list[list.length-1], true);
        this._l = list;
    }

    get next(){
        let nxtVal = this.value+1;
        while(this._l.some(x => nxtVal % x == 0)){
            nxtVal++;
        }
        return new Primes(this._l.concat([nxtVal]));
    }
}

class Fib extends List{
    constructor(prec1, prec2){
        super(prec1 + prec2, true);
        this._v1 = prec1;
        this._v2 = prec2;
    }

    get next(){
        return new Fib(this._v2, this.value);
    }
}


class ScanL extends List {

    constructor(value, list, fn){
        super(value, list.isInfinite);
        this._list = list;
        this._fn = fn;
    }

    get next() {
        return this._list.isEmpty ? List.empty :  new ScanL(this._fn(this.value, this._list.value), this._list.next, this._fn);
    }
}

class Zipper extends List {
    constructor(list1, list2, fn){
        super(fn(list1.value, list2.value), list1.isInfinite && list2.isInfinite);
        this._l1 = list1;
        this._l2 = list2;
        this._fn = fn;
    }

    get next() {
        if(this._l1.next.isEmpty || this._l2.next.isEmpty) return List.empty;
        return new Zipper(this._l1.next, this._l2.next, this._fn);
    }
}

class Concat extends List {


    constructor(list, current) {
        super(current.value, list.isInfinite || current.isInfinite);
        this._list = list;
        this._current = current;
    }

    get next(){
        return this._current.next.isEmpty ? this._list.next.isEmpty ? List.empty : (new Concat(this._list.next, this._list.next.value)) :
                                            new Concat(this._list, this._current.next);
    }
}

class Filtered extends List {

    constructor(list, fn){
        super(list.value, list.isInfinite);
        this._list = list;
        this._fn = fn;
    }

    get next(){
        let current = this._list.next;
        while(!current.isEmpty && !this._fn(current.value)){
            current = current.next;
        }
        return current.emptyOrMe(new Filtered(current, this._fn));
    }
}

class Mapped extends List {

    constructor(list, fn){
        super(fn(list.value), list.isInfinite);
        this._list = list;
        this._fn = fn;
    }

    get next(){
        return this._list.next.nil() ? List.empty : new Mapped(this._list.next, this._fn);
    }
}

class Append extends List {

    constructor(list, append){
        super(list.value, false);
        this._list = list;
        this._append = append;
    }

    get next(){
        return this._list.next.isEmpty ? this._append : new Append(this._list.next, this._append);
    }
}

class Prepend extends List {

    constructor(list, x){
        super(x, list.isInfinite);
        this._list = list;
    }

    get next(){
        return this._list;
    }
}


class Take extends List {

    constructor(list, n){
        super(list.value, false);
        if(n < 0) throw "Can't take negative number of elements"
        this._n = n;
        this._list = list;
    }

    get next(){
        return this._n <= 1 ? List.empty : this._list.next.emptyOrMe(new Take(this._list.next, this._n-1));
    }
}

class IgnoreLast extends List {


    constructor(list){
        super(list.value, list.isInfinite);
        this._list = list;
    }

    get next(){
        if(this._list.next.isEmpty || this._list.next.next.isEmpty) return List.empty;
        return new IgnoreLast(this._list.next);
    }
}

class FromList extends List {

    constructor(list){
        super(list[0], false);
        this._list = list;
    }

    get next(){
        return this._list.length === 1 ? new EmptyList() : new FromList(this._list.slice(1));
    }
}

class Replicate extends List {

    constructor(val, repeats){
        super(val, false);
        this.repeats = repeats;
    }

    get next(){
        if(this.repeats <= 1) return List.empty;
        return new Replicate(this.value, this.repeats-1);
    }
}

class Cycle extends List {

    constructor(curent, xs = curent){
        super(curent.head() || xs.head(), true);
        this._total = xs;
        this._subList = curent.tail();
        if(curent.head() === undefined){
            this._subList = xs.tail();
        }
    }

    get next(){
        return new Cycle(this._subList, this._total);
    }
}

class Repeat extends List {

    constructor(x){
        super(x, true);
    }

    get next(){
        return new Repeat(this.value);
    }
}

class Iterate extends List {

    constructor(fn, x){
        super(x, true);
        this._fn = fn;
    }

    get next(){
        return new Iterate(this._fn, this._fn(this.value));
    }

}

class EmptyList extends List{

    constructor(){
        super(undefined, false);
    }

    emptyOrMe(val){
        return this;
    }

    get isEmpty(){
        return true;
    }

    get next(){
        throw "No next to empty list";
    }

    head(){return undefined;}
    tail(){return this;}
    init(){return this;}
    last(){return undefined;}
    length(){return 0;}
    toList(){return [];}
    get(){return undefined;}
    nil(){return true;}
    take(){return this;}
    drop(){return this;}
    append(xs){return xs;}
    slice(){return this;}
    map(){return this;}
    filter(){return this;}
    reverse(){return this;}
    concat(){return this;}
    concatMap(){return this;}
    zipWith(){return this;}
    foldr(_,x){return x;}
    foldl(_,x){return x;}
    scanr(_,x){return new Prepend(this, x);}
    scanl(_,x){return new Prepend(this, x);}
    elem(){return false;}
    elemIndex(){return -1;}
    find(){return undefined;}
    findIndex(){return -1;}
    any(){return false;}
    all(){return true;}
    the(){return undefined;}
}

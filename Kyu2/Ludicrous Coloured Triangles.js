const POW_3_UPTO = 17; //max n = 2*3^17 = 258 280 326
const POWERS_OF_3 = [];
for (let i=0; i<=POW_3_UPTO; i++) POWERS_OF_3.push(3**i);
const POW_3_MAX_IND = POWERS_OF_3.length-1;
const HALF_WAY_POW_3_IND = (POWERS_OF_3.length/2)|0;
const HALF_WAY_POW_3_VAL = POWERS_OF_3[HALF_WAY_POW_3_IND];
const Q1_IND = (POWERS_OF_3.length/4)|0;
const Q3_IND = Q1_IND*3;
const Q1_VAL = POWERS_OF_3[Q1_IND];
const Q3_VAL = POWERS_OF_3[Q3_IND];

const getLastPowerOf3 = function(a) {
    let iStart;
    if (a>HALF_WAY_POW_3_VAL) {
        iStart = a<Q3_VAL ? Q3_IND : POW_3_MAX_IND;
    } else {
        iStart = a<Q1_VAL ? Q1_IND : HALF_WAY_POW_3_IND;
    }
    let p3;
    for (let i=iStart; i>=0; i--) {
        p3 = POWERS_OF_3[i];
        if (p3<=a) return p3
    }
    //hopefully never come here :=)
};

const MEMO_MAX_LEN = 7;
const MEMO = {"R": 0, "G": 1, "B": 2};

//R=0, G=1, B=2
const toNum = ch=>ch==='R' ? 0 : (ch==='G' ? 1 : 2);
const toChar = num=>num===0 ? 'R' : (num===1 ? 'G' : 'B');

var triangle = function(origRow, print=false) {
    if (origRow.length===0) return "";
    
    var getFromMemo = function(start, end) {
        let row = origRow.substring(start, end+1);
        if (row in MEMO) return MEMO[row];
        return null;
    };
    
    var putToMemo = function(start, end, val) {
        let row = origRow.substring(start, end+1);
        MEMO[row] = val;
    };
    
    /** Sum 1*row_start - 1*row_{start+1} -...+/-1*row_end
    *   These sort of rows appear in Pascal mod 3 at inds 3^h-1
    *      and 2*3^h-1 where it appears twice (in the middle there are two 1's).
    */
    var calcAlterSum = function(start, end) {
        var s = 0, x=1;
        for (let i=start; i<=end; i++) {
            s += x*toNum(origRow.charAt(i));
            x = -x;
        }
        return s;
    };
    
    /** Gives sum j=start..end binom(N, j)*row_{start+j} (mod 3),
     *   where N = end-start
     *   when row's chars converted to nums
     */
    var t = function(start, end) {
        var N = end-start;
        if (N<=MEMO_MAX_LEN) {
            let fromMemo = getFromMemo(start, end);
            if (fromMemo!==null) return fromMemo;
        }
        //if (N===0) return toNum(origRow.charAt(start)); //not needed since, MEMO takes care
        var p = getLastPowerOf3(N);
        var twoP = 2*p;
        if (N===p-1) {
            return ((calcAlterSum(start, end)%3)+1)%3;
        } else if (N==twoP-1) {
            let firstAltSum = (calcAlterSum(start, start+p-1))%3;
            let secondAltSum = (calcAlterSum(start+p, end))%3;
            return (((firstAltSum+secondAltSum)%3)+3)%3;
        } else if (N<twoP) {
            let d = N-p;
            return (t(start, start+d) + t(start+p, end))%3;
        } else {
            let d = N-twoP;
            let res = ((( t(start, start+d)
                       - t(start+p, start+p+d)
                       + t(start+twoP, end) )%3)+3)%3;
            if (N<=MEMO_MAX_LEN) putToMemo(start, end, res);
            return res;
        }
    };
    var finalRes = t(0, origRow.length-1);
    return toChar( origRow.length%2 ? finalRes : (3-finalRes)%3 ) ;
};

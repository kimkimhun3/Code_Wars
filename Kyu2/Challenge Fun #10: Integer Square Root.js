function integerSquareRoot(s) {
  let mul = (a, b) => {
    let result = a.split``.map(d => d * b);
    for (let i = result.length - 1; i > 0; i--) {
      result[i - 1] += ~~(result[i] / 10);
      result[i] %= 10;
    }
    result = result.join``.replace(/^0+/, '');
    return result.length ? result : '0';
  }, sub = (a, b) => {
    a = '0'.repeat(Math.max(0, b.length - a.length)) + a;
    b = '0'.repeat(Math.max(0, a.length - b.length)) + b;
    let result = a.split``.map((d, i) => d - b[i]);
    for (let i = result.length - 1; i > 0; i--) if (result[i] < 0) {
      result[i] += 10;
      result[i - 1]--;
    }
    result = result.join``.replace(/^0+/, '');
    return result.length ? result : '0';
  }, lte = (a, b) => {
    a = '0'.repeat(Math.max(0, b.length - a.length)) + a;
    b = '0'.repeat(Math.max(0, a.length - b.length)) + b;
    if (a === b) return true;
    for (let i = 0; i < a.length; i++) {
      if (a[i] < b[i]) return true;
      else if (a[i] > b[i]) return false;
    }
  };
  if (s.length % 2) s = '0' + s;
  let result = '0', carry = '0';
  for (let i = 0; i < s.length; i += 2) {
    carry += s[i] + s[i + 1];
    let doubleCurrent = mul(result, '2'), j = 0;
    while (lte(mul(doubleCurrent + j, '' + j), carry) && j < 10) j++;
    result += --j;
    carry = sub(carry, mul(doubleCurrent + j, '' + j));
  }
  result = result.replace(/^0+/, '');
  return result.length ? result : '0';
}

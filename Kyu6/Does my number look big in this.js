function narcissistic( value ) {
  var str = value.toString();
  var sum = 0;
  for (var it = 0; it < str.length; ++it)
  {
    sum += Math.pow(parseInt(str[it]), str.length);
  }
  if (sum == value)
  {
    return true;
  }
  else
  {
    return false;
  }
}

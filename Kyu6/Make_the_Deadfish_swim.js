function parse( data ) {  
  var v = 0, ret = []
  for (var c of data) {
    switch (c) {
      case 'i' : v++;         break;
      case 'd' : v--;         break;
      case 's' : v=v*v;       break;
      case 'o' : ret.push(v); break;
    }
  }
  return ret;
}
// parse("iiisdoso");
parse("xixiixsdxxoso");

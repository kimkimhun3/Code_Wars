function playFlou(gameMap) {
  var yy, xx, cy=[], cx=[];
  var dy = [-1,0,1,0];
  var dx = [0,1,0,-1];
  var dn = ["Up","Right","Down","Left"];
  var arr = gameMap.split("\n").map(x=>x.split("").map(y=>y!="."));
  var hig = arr.length-2;
  var wid = arr[0].length-2;
  var all = wid*hig;
  
  for (yy=1; yy<=hig; yy++)
  for (xx=1; xx<=wid; xx++) {
    if (arr[yy][xx]) {
      cy.push(yy);
      cx.push(xx);
    }
  }
  
  var len = cy.length;
  var cur = len;
  var ay=[], ax=[];
  
  function loop(k) {
    if (k>=len) return (cur==all?[]:false);
    
    var a, y=ay[k], sy=[];
    var b, x=ax[k], sx=[];
    var d, i, ret;
    
    for (var r=0; r<4; r++) {
      if (arr[y+dy[r]][x+dx[r]]) continue;
      [d,a,b] = [r,y,x];
      
      while(true) {
        do {
          a += dy[d];
          b += dx[d];
          arr[a][b] = true;
          sy.push(a);
          sx.push(b);
        }
        while (!arr[a+dy[d]][b+dx[d]]);
        
        d = (d+1)%4;
        if (arr[a+dy[d]][b+dx[d]]) {
          cur += sy.length;
          ret = loop(k+1);
          
          for (i=0; i<sy.length; i++)
            arr[sy[i]][sx[i]] = false;
          
          cur -= sy.length;
          [sy,sx] = [[],[]];
          
          if (typeof ret=="object") return [[y-1,x-1,dn[r]], ...ret];
          break;
        }
      }
    }
    return false;
  }
  
  function choose(exp) {
    if (exp.length==len) {
      [ay,ax] = [[],[]];
      for (var i=0; i<exp.length; i++) {
        ay.push(cy[exp[i]]);
        ax.push(cx[exp[i]]);
      }
      return loop(0);
    }
    else for (var i=0; i<len; i++) {
      if (exp.indexOf(i)==-1) {
        var ret = choose([...exp,i]);
        if (typeof ret=="object") return ret;
      }
    }
    return false;
  }
  
  return choose([]);
}

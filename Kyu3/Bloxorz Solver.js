function bloxSolver(arr){
  let end,H=arr.length,W=arr[0].length,result=false
  const q = [{rc:0,moves:''}], ext = new Set(), 
  validCoords=(coords)=> !ext.has(coords.toString()) && coords.every(([r,c])=> 0 <= c && c < W && 0 <= r && r < H && '1BX'.includes(arr[r][c]))
  arr.forEach((x,r)=>{let a=x.indexOf('B'),b=x.indexOf('X'); if (~a) q[0].rc = [[r,a]]; if (~b) end = [r,b]})
  while(q.sort((a,b)=>a.moves.length-b.moves.length).length) {
    const p = q.shift(), [r,c] = p.rc[0], [y,x] = p.rc.length === 1 ? [-1,0] : p.rc[1], 
    data = y ===-1 ? [['U',[r-2,c],[r-1,c]], ['D',[r+1,c],[r+2,c]], ['L',[r,c-1],[r,c-2]], ['R',[r,c+2],[r,c+1]]] :
           c === x ? [['U',[r-1,c]],         ['D',[y+1,c]],         ['L',[r,c-1],[y,c-1]], ['R',[r,c+1],[y,c+1]]] :
                     [['U',[r-1,c],[r-1,x]], ['D',[r+1,c],[r+1,x]], ['L',[r,x-1]],         ['R',[r,c+1]]]
    if (p.rc.toString() === end.toString() &&(result===false || p.moves.length < result.length)) result = p.moves
    ext.add(p.rc.toString())
    for (let [d,...coords] of data)
      if (validCoords(coords)) 
        q.push({rc:coords.slice(), moves:p.moves+d})
  }
  return result
}

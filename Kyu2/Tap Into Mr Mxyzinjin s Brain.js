var attempt = 1
function crack(login) {
  const pw = Array(32).fill(' ')
  const getTimeSummary = p => {
    if (login(p)) return true
    const t = process.hrtime()
    for (let i = 0; i < 10; i++) login(p)
    return process.hrtime(t)[1]          
  }
  for (let int = 0; int < 32; int++) {          
    const data = Array(10).fill(Infinity)                       
    for (let i = 0; i < 1e2; i++)  
      for (let d = 0; d < 10; d++) {          
        pw[int] = d
        const log = getTimeSummary(pw.join``)
        if (log === true) return pw.join``                    
        if (data[d] > log) data[d] = log
      }
    pw[int] = data.indexOf(Math.max(...data))
    console.log(pw.join``)
  }
  console.log(`\nattempt number ${++attempt}:\n`)
  return crack(login)
}

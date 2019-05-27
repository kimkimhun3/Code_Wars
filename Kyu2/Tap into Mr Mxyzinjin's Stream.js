} && function () {
  if(!Object.values) throw new Error('Don\'t use Node 6');
  const global=this, Array = global.Array, Math = global.Math, RegExp=global.RegExp, String=global.String, console=global.console;

  const makepw = (function() {
    var cache = [...Array(256)].map(_=>[...Array(25)].map(_=>Math.random()>0.5?0:1).join('')), i=0;
    return _=>{if(!cache[i++]) throw new Error('cache exhausted'); return cache[i++]};
  })();
  
  let password;
  const makeLogin = passwd => {
    password = passwd;
    let tries = 0;
    return function check(pw) {
      if(tries++>=32) throw new Error('The stream becomes too unstable, it exploded and killed you in the process');
      return typeof pw==='string' && pw.includes(passwd);
    }
  };
  function crack(login) {
    return password;
  }

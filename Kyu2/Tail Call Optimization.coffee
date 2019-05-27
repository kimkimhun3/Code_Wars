class Delayed
  constructor: (@k,@a)->

tco = (fd)->
  fns={}
  fd.map ([k,a,b])->
    fns[k]=new Function a...,b
    global[k]=(a...)->
      new Delayed k,a
    (a...)->
      d=fns[k] a...
      while d instanceof Delayed
        d=fns[d.k] d.a...
      d

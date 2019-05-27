permutes = ( arr )->
  p = arr[..]
  [i,l,o]=[1,p.length,[p[..]]]
  c = Array(l).fill 0

  while i < l
    if c[i] < i
      k = i%2 and c[i]
      [p[i],p[k]] = [p[k],p[i]]
      ++c[i]
      i = 1
      o.push p[..]
    else
      c[i] = 0
      ++i
  o
ops =
  '+': (a,b)->a+b
  '-': (a,b)->a-b
  '*': (a,b)->a*b
  '/': (a,b)->a/b

equalTo24 = (u...) ->
  ns = permutes u
  for u in ns
    for o1, f1 of ops
      for i in [0..2]
        v = u[...i].concat [f1 u[i],u[i+1]].concat u[i+2..]
        for o2, f2 of ops
          for j in [0..1]
            w = v[...j].concat [f2 v[j],v[j+1]].concat v[j+2..]
            for o3, f3 of ops
              r = f3 w[0],w[1]
              if 1e-12 > Math.abs r-24
                v = u[...i].concat ["(#{u[i]}#{o1}#{u[i+1]})"].concat u[i+2..]
                w = v[...j].concat ["(#{v[j]}#{o2}#{v[j+1]})"].concat v[j+2..]
                return "#{w[0]}#{o3}#{w[1]}"
  "It's not possible!"

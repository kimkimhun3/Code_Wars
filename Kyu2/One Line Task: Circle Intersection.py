from numpy import*;circleIntersection=lambda a,b,r:(lambda s:int(max(0,r*r*(s-sin(s)))))(2*arccos(hypot(*array(a)-b)/2/r))

from math import sin, cos, acos, radians, degrees

def solve(targetZenith, lat, dec):
    cosH = (cos(radians(targetZenith)) - sin(radians(lat)) * sin(radians(dec))) / (cos(radians(lat)) * cos(radians(dec)))
    if cosH < -1.0 or cosH > 1.0: return None
    return degrees(acos(cosH))

lat = 24.8607
dec = 14.0 # approximate declination for April 27
fajr = solve(108.0, lat, dec)
sunrise = solve(90.8333, lat, dec)
print("fajr H:", fajr)
print("sunrise H:", sunrise)

'''
Created on Mar 16, 2012

@author: amokashi
'''
from numbthy import *
import sys
n = 50515093
si = []
s = 290797
s = powmod(s, 2, n)

for i in xrange(0, 2000000000):
    if i % 1000000 == 0:
        print i
    si.append(s)
    min = sys.maxint
    sum = 0
    j = i
    while j >= 0:
        if si[j] < min:
            min = si[j]
        sum += min
        j = j - 1
    if i == 0:
        mi = sum
    else:
        mi = sum + mi
    s = powmod(s, 2, n)

print mi
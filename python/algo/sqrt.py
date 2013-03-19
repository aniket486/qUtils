#!/usr/bin/env python

def sqrt(num, precision=0.01):
    sqrte = 1
    while (mod(sqrte * sqrte - num) > precision):
        sqrte = (sqrte + num / sqrte) / 2
    return sqrte

def mod(n):
    if n < 0: return -n
    else: return n

def main():
    assert 3 == sqrt(10)
    assert 31 == sqrt(1000)
    assert 1 == sqrt(1)
    assert 3 == sqrt(9)

if __name__ == '__main__':
    main()

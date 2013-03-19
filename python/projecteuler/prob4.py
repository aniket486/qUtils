'''
Created on Mar 16, 2012

@author: amokashi
'''

i = 999
j = 999

def isPalindromeNum(num):
    isPalindrome(str(num))
    
def isPalindrome(phrase):
    print phrase
    phrase_letters = [c for c in phrase]
    return (phrase_letters == phrase_letters[::-1])

count = 0
while(not isPalindromeNum(i*j)):
    if count != 100:
        j = j - 1
    


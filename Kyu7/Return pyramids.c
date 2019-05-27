#include <stdio.h>
#include <string.h>
#include <stdlib.h>

char* pyramid(int n) {
    char *p;
    if (n == 0) {
        p = calloc(2, sizeof(char));
        p = "\n";
        return p;
    }
    int len = 3*n*(n+1)/2;
    p = calloc(len + 1, sizeof(char));
    for (int i = 0; i < n; i++) {
        for (int j = n-i-1; j-- > 0;)
            *p++ = ' ';
        *p++ = '/';
        char delim = i == n-1 ? '_' : ' ';
        for (int j = i * 2; j-- > 0;)
            *p++ = delim;
        *p++ = '\\';
        *p++ = '\n';
    }
    *p = '\0';
    return p - len;
}

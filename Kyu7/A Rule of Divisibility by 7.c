#include <stdlib.h>

int* seven (long long m)
{
    int* res = (int*)malloc (2 * sizeof (int));

    int step = 0;

    while (m > 99)
    {
        m = m / 10 - 2 * (m % 10);
        ++step;
    }

    res[0] = m;
    res[1] = step;

    return res;
}

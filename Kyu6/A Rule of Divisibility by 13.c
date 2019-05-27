long long thirt(long long n)
{
    int remainders[6] = {1, 10, 9, 12, 3, 4};
    int iRem = 0;
    long long m = n, sum = 0;
    
    while (n > 0)
    {
      sum += (n % 10) * remainders[iRem++];
      n /= 10;
      if (iRem >= 6) iRem = 0;
    }
    
    return (sum == m)?(m):(thirt(sum));
}

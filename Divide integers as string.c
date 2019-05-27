#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define max(a,b) (((a)>(b))?(a):(b))

int remove_last_zeros(char *difference)
{
  int len_a = strlen(difference);
  for(int i = len_a-1; i>0; i--)
  {
    if(difference[i] == '0')
    {
      difference[i] = 0;
    }
    else
    {
      break;
    }
  }  
  return 0;
}

int compare_strings(char *a, char *b)
{
  if (strlen(a) > strlen(b))
    return 1;
  else if (strlen(a) < strlen(b))
    return -1;
  else
    return strcmp(a,b);
}

static int strrev(char *s)
{
  char tmp;
  int len = strlen(s);
  for(int i = 0; i < len/2; i++)
  {
    tmp = s[i];
    s[i] = s[len-i-1];
    s[len-i-1] = tmp;
    
  }
  return 0;
}

char *subtract_strings(char *a, char *b)
{
  int len_a = strlen (a), len_b = strlen(b);
  
  char *difference = calloc(len_a + 1, sizeof(char));
  char *a_rv = calloc(len_a + 1, sizeof(char));
  char *b_rv = calloc(len_a + 1, sizeof(char));
  
  strcpy (a_rv, a);
  strcpy (b_rv, b);

  strrev(a_rv);
  strrev(b_rv);

  if(len_a > len_b)
    memset (b_rv + len_b, '0', len_a - len_b);
  
  for (int i = 0, c = 0; i < len_a; i++)
  {
    if (a_rv[i] >= b_rv[i] + c)
    {
      difference[i] = '0' + a_rv[i] - b_rv[i] - c;
      c = 0;
    }
    else
    {
      difference[i] = '0' + (10 + (a_rv[i] - b_rv[i] - c));
      c = 1;
    }
  }
  
  remove_last_zeros(difference);
  free(a_rv); free(b_rv);
  strrev(difference);
  return difference;
}

char *multiply_strings(char *a, char b)
{  
  int len_a = strlen(a);
  char *a_rv = calloc(len_a + 1, sizeof(char));
  char *product = calloc(strlen(a) + 2, sizeof(char));
  char b_val = b -'0';
  
  //special case where b == 'a' (10 numeric)
  if(b == '9' + 1)
  {
    strcpy(product, a);
    int len = strlen(product);
    product[len] = '0';
    product[len+1] = 0;
    return product;
  }
  
  strcpy (a_rv, a);
  strrev (a_rv);
  
  char c = 0;
  int a_val;
  int i;
  for(i=0; i < len_a; i++)
  {
    a_val = a_rv[i] - '0';
    a_val = a_val * b_val + c;
    c = a_val / 10;
    product[i] = '0' + a_val % 10;
  }
  if (c) 
    product[i] = '0' + c;
  
  free(a_rv);
  remove_last_zeros(product);
  strrev (product);
  return product;
}

char **divide_strings(char *a, char *b)
{
  char **result = calloc(2 , sizeof(char *));
  int len_a = strlen(a);
  int len_b = strlen(b);
  char *quotient = calloc(max(len_a,len_b) + 1, sizeof(char));
  char *dividend = calloc(len_b + 2, sizeof(char));
  
  if (compare_strings(a,b) < 0)
  {
      strcpy(quotient, "0");
      strcpy(dividend, a);
      *(result+1) = dividend;
      *result = quotient;
      return result;
  }
  else if (compare_strings(a,b) == 0)
  {
      strcpy(quotient, "1");
      strcpy(dividend, "0");
      *(result+1) = dividend;
      *result = quotient;
      return result;
  }
  
  
  
  for(int i = 0; i < len_a - len_b + 1 ; i++)
  {
    if (i == 0)
    {
      strncpy(dividend, a, len_b);
    }
    else
    {
      int n = strlen(dividend);
      dividend[n] = a[i+len_b-1];
      dividend[n+1] = 0;
    }
    
     if(dividend[0] == '0')
     {
       quotient[i] = '0';
       strcpy(dividend, "");
       continue;
     }
    
    for(char j = 0; j <= 10; j++)
    {
      int cmp;
      char *tmp = multiply_strings(b,j + '0');
      if ((cmp = compare_strings(dividend, tmp)) <= 0)
      {
        
        if(j >= 1)
        {
          if( cmp != 0)
          {
            quotient[i] = '0' + j-1;
            free(tmp);
            tmp = multiply_strings(b,j + '0'-1);
          }
          else
          {
            quotient[i] = '0' + j;
          }
          char *tmp2 = subtract_strings(dividend, tmp);
          if(tmp2[0] != '0')
            strcpy(dividend, tmp2);
          else
            strcpy(dividend, "");
          free(tmp2);
        }
        free(tmp);
        break;
      }
      free(tmp);
    }
  }
  if(strlen(dividend) == 0)
    strcpy(dividend, "0");
  *(result+1) = dividend;
  
  strrev(quotient);
  remove_last_zeros(quotient);
  strrev(quotient);
  *result = quotient;
  
  return result;
}

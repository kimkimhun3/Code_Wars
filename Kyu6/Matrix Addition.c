#include <stdlib.h>
#include <stddef.h>

int* matrix_addition(size_t n, int matrix_a[n][n], int matrix_b[n][n]) {
  int i;
  int *result = malloc(n * n * sizeof(int *) );
  int *a, *b;
  
  a = matrix_a;
  b = matrix_b;
  for(i=0; i<n*n; i++) result[i] = a[i] + b[i];
  return result;
}

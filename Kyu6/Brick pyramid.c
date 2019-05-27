#include <math.h>

double weight(int row, int pos) {
  if (pos>row || (row ==0 && pos == 0)) {
    return 0;
  }
  double h = (double)(pow(2,row)-1)/pow(2,row);
  if (pos == 0 || pos == row) {
    return h;
  }
  return (row * (row + 1) / 2 - h * 2) / (row - 1);;
}

template<std::size_t S>
long long solve(std::array<int, S> input) {
  constexpr int maxControl{10};
  
  long long result{S}, evens{0};
 
  int num[100001];
  std::fill(num, num + 100001, 0);
  
  auto add = [&] (int j) {
    auto& arg = num[input[j]];
    arg ++;
    if (arg > 1) evens += (arg % 2) ? -1 : 1;
    return evens ? 0 : 1;
  };
 
  auto minus = [&] (int j) {
    auto& arg = num[input[j]];
    arg --;
    if (arg > 0) evens += (arg % 2) ? -1 : 1;
    return evens ? 0 : 1;
  };
 
 
  int right{0}, left{0};
  for (int iControl = 0; iControl < maxControl; ) {
    long long old = result;
    
    if (right < S) add(right++);
    if (right < S) result += add(right++);

    while (right < S) result += add(right++) + minus(left++);
   
    if (left >= 0) add(--left);
    if (left >= 0) result += add(--left);

    while (left > 0) result += add(--left) + minus(--right);
   
    if (old == result) iControl++;
    else iControl = 0;
  }
 
  return result;

}

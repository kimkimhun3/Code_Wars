function arrayPlusArray(arr1, arr2) {
  var len1=arr1.length;
  var len2=arr2.length;
  var sum=0;
  var i=0;
  for(i=0;i<len1;i++)
  sum+=arr1[i];
  for(i=0;i<len2;i++)
  sum+=arr2[i];
  return sum;

}

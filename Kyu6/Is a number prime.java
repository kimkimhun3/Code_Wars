public class Prime 
{
  public static boolean isPrime(int num) 
  {
    if(num==2 || num==3)  return true;
    else if(num<2 || num%2==0)  return false;
    else
    {
      int div = (int)Math.sqrt(num);
      int flag=0;
      for(int i=2;i<=div;i++)
      {
        if(num%i==0)  
          flag=1;
      }
      if(flag==1)  return false;
      else  return true;
    }
  }
}

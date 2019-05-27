public class FindOdd {

    public static int findIt(int[] a) {
        int count;
        int odd = 0;
        for (int i = 0; i < a.length; i++) {
            count = 0;
            int x = a[i];
            for (int j = 0; j < a.length; j++)
            {
                if (x == a[j]) count++;
            }
            if (count % 2 == 1) odd = a[i];


        }return odd;
    }
}

package hr.fer.bernardcrnkovic.mtsp.algo;

public class SwapSubSeq {

    public static void swapSubsequences(int[] array, int a1, int a2, int b1, int b2) {
        int lengthA = a2 - a1 + 1;
        int lengthB = b2 - b1 + 1;

        int[] temp = new int[lengthA];

        // Store elements of subsequence A in a temporary array
        for (int i = 0; i < lengthA; i++) {
            temp[i] = array[a1 + i];
        }

        // Shift elements of subsequence B to the left
        for (int i = b1; i <= b2; i++) {
            array[a1 + i - b1] = array[i];
        }

        // Copy elements from the temporary array to subsequence B
        for (int i = 0; i < lengthA; i++) {
            array[b2 - lengthA + 1 + i] = temp[i];
        }
    }

    public static void printArray(int[] array) {
        for (int value : array) {
            System.out.print(value + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        System.out.println("Original array:");
        printArray(array);

        int a1 = 1, a2 = 4;
        int b1 = 7, b2 = 9;

        swapSubsequences(array, a1, a2, b1, b2);

        System.out.println("Array after swapping subsequences:");
        printArray(array);
    }
}

package util;

import java.util.Scanner;

public class Input {
    private final Scanner sc = new Scanner(System.in);

    public String str(String label) {
        System.out.print(label + ": ");
        return sc.nextLine().trim();
    }

    public int intVal(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim();
            try { return Integer.parseInt(s); } catch (Exception ignored) {}
            System.out.println("Valoare invalida. Incearca iar.");
        }
    }

    public long longVal(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim();
            try { return Long.parseLong(s); } catch (Exception ignored) {}
            System.out.println("Valoare invalida. Incearca iar.");
        }
    }

    public double doubleVal(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim();
            try { return Double.parseDouble(s); } catch (Exception ignored) {}
            System.out.println("Valoare invalida. Incearca iar.");
        }
    }
}
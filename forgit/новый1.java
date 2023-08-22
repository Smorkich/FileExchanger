import java.util.Scanner;

public class SimplestRomanCalculator {

    // Массивы для хранения римских символов и их эквивалентных арабских чисел
    private static final char[] romanSymbols = {'I', 'V', 'X', 'L', 'C', 'D', 'M'};
    private static final int[] arabicValues = {1, 5, 10, 50, 100, 500, 1000};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите выражение с римскими или арабскими числами: ");
		System.out.println("Я сосал ");
        String expression = scanner.nextLine();

        try {
            int result = evaluateExpression(expression);
            System.out.println("Результат: " + result);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        scanner.close();
    }

    public static int evaluateExpression(String expression) {
        String[] tokens = expression.split(" ");
        if (tokens.length != 3) {
            throw new IllegalArgumentException("Некорректное выражение");
        }

        int operand1 = parseToken(tokens[0]);
        int operand2 = parseToken(tokens[2]);
        String operator = tokens[1];

        int result;

        switch (operator) {
            case "+":
                result = operand1 + operand2;
                break;
            case "-":
                result = operand1 - operand2;
                break;
            case "*":
                result = operand1 * operand2;
                break;
            case "/":
                if (operand2 == 0) {
                    throw new IllegalArgumentException("Деление на ноль недопустимо");
                }
                result = operand1 / operand2;
                break;
            default:
                throw new IllegalArgumentException("Некорректный оператор");
        }

        return result;
    }

    public static int parseToken(String token) {
        try {
            return Integer.parseInt(token); // Пытаемся распарсить как арабское число
        } catch (NumberFormatException e) {
            // Если не удалось преобразовать в число, попробуем как римскую цифру
            return convertRomanToArabic(token);
        }
    }

    public static int convertRomanToArabic(String romanNumeral) {
        int result = 0;
        int prevValue = 0;

        for (int i = romanNumeral.length() - 1; i >= 0; i--) {
            int currentValue = getValueOfRomanSymbol(romanNumeral.charAt(i));

            if (currentValue < prevValue) {
                result -= currentValue;
            } else {
                result += currentValue;
            }

            prevValue = currentValue;
        }

        return result;
    }

    public static int getValueOfRomanSymbol(char symbol) {
        for (int i = 0; i < romanSymbols.length; i++) {
            if (romanSymbols[i] == symbol) {
                return arabicValues[i];
            }
        }
        throw new IllegalArgumentException("Некорректный символ римской цифры: " + symbol);
    }
}
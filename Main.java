
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        CouponManager couponManager = new CouponManager();

        Scanner sc = new Scanner(System.in);

        // Load coupons when application starts
        couponManager.loadCoupons();

        int choice;

        do {

            System.out.println("\n===== COUPON & DISCOUNT MANAGER =====");

            System.out.println("1. Add Coupon");
            System.out.println("2. Search Coupon");
            System.out.println("3. Display Coupons");
            System.out.println("4. Apply Best Discount");
            System.out.println("5. Generate Coupon Report");
            System.out.println("6. Exit");

            System.out.print("Enter your choice: ");

            while (!sc.hasNextInt()) {
                System.out.println("Enter a valid menu number.");
                sc.nextLine();
                System.out.print("Enter your choice: ");
            }

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    addCoupon(sc, couponManager);
                    break;

                case 2:
                    searchCoupon(sc, couponManager);
                    break;

                case 3:
                    couponManager.displayCoupons();
                    break;

                case 4:
                    applyBestDiscount(sc, couponManager);
                    break;

                case 5:
                    couponManager.generateReport();
                    break;

                case 6:
                    couponManager.saveCoupons();
                    System.out.println(
                            "Thank you for using the application.");
                    break;

                default:
                    System.out.println(
                            "Invalid choice. Try again.");
            }

        } while (choice != 6);

        sc.close();
    }

    // ADD COUPON
    public static void addCoupon(
            Scanner sc, CouponManager manager) {

        System.out.println("\n--- ADD COUPON ---");

        System.out.print("Enter coupon code: ");
        String code = sc.nextLine();

        System.out.print(
                "Enter discount type (PERCENTAGE/FLAT): ");

        String type = sc.nextLine();

        System.out.print("Enter discount value: ");
        double value = readPositiveDouble(sc);

        LocalDate startDate = readDate(
                sc, "Enter start date (YYYY-MM-DD): ");

        LocalDate expiryDate = readDate(
                sc, "Enter expiry date (YYYY-MM-DD): ");

        System.out.print("Enter usage limit: ");
        int uses = readPositiveInt(sc);

        boolean added = manager.addCoupon(
                code, type, value,
                startDate, expiryDate, uses);

        if (added) {

            String discount;

            if (type.equalsIgnoreCase("PERCENTAGE")) {
                discount = value + "%";
            } else {
                discount = "Flat " + value;
            }

            System.out.println(
                    "Coupon added: "
                    + code.trim().toUpperCase()
                    + " | " + discount);
        }
    }

    // SEARCH COUPON
    public static void searchCoupon(
            Scanner sc, CouponManager manager) {

        System.out.println("\n--- SEARCH COUPON ---");

        System.out.print("Enter coupon code: ");

        String code = sc.nextLine();

        Coupon coupon = manager.searchCoupon(code);

        if (coupon == null) {

            System.out.println(
                    "Coupon " + code.toUpperCase()
                    + " not found.");

        } else {

            System.out.println("Coupon found: " + coupon);

            System.out.println(
                    "Currently valid: "
                    + (manager.validateCoupon(coupon)
                    ? "Yes" : "No"));
        }
    }

    // APPLY BEST OR SPECIFIC DISCOUNT
    public static void applyBestDiscount(
            Scanner sc, CouponManager manager) {

        System.out.println("\n--- APPLY DISCOUNT ---");

        System.out.print("Enter bill amount: ");

        double bill = readPositiveDouble(sc);

        System.out.print(
                "Enter coupon code (or ALL for best discount): ");

        String code = sc.nextLine().trim();

        CouponManager.DiscountResult result;

        if (code.equalsIgnoreCase("ALL")) {

            // Greedy selection of best coupon
            result = manager.applyBestDiscount(bill);

        } else {

            // Apply the specified coupon
            result = manager.applyBestDiscount(bill, code);
        }

        if (!result.isCouponApplied()) {

            System.out.println("No discount applied.");

            System.out.println("Final bill: " + bill);

        } else {

            System.out.println(
                    "Best coupon selected: "
                    + result.getCoupon().getCode());

            System.out.println(
                    "Best discount applied: "
                    + result.getDiscount());

            System.out.println(
                    "Final bill: "
                    + result.getFinalBill());

            System.out.println(
                    "Remaining uses: "
                    + result.getCoupon().getRemainingUses());
        }
    }

    // READ POSITIVE DOUBLE
    private static double readPositiveDouble(Scanner sc) {

        while (true) {

            if (sc.hasNextDouble()) {

                double value = sc.nextDouble();
                sc.nextLine();

                if (Double.isFinite(value) && value > 0) {
                    return value;
                }

            } else {
                sc.nextLine();
            }

            System.out.print(
                    "Enter a valid positive number: ");
        }
    }

    // READ POSITIVE INTEGER
    private static int readPositiveInt(Scanner sc) {

        while (true) {

            if (sc.hasNextInt()) {

                int value = sc.nextInt();
                sc.nextLine();

                if (value > 0) {
                    return value;
                }

            } else {
                sc.nextLine();
            }

            System.out.print(
                    "Enter a valid positive integer: ");
        }
    }

    // READ DATE
    private static LocalDate readDate(
            Scanner sc, String message) {

        while (true) {

            System.out.print(message);

            String input = sc.nextLine();

            try {

                return LocalDate.parse(input);

            } catch (DateTimeParseException e) {

                System.out.println(
                        "Invalid date. Use YYYY-MM-DD format.");
            }
        }
    }
}

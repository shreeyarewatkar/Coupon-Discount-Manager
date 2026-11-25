
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public class CouponManager {

    private static final String FILE_NAME = "coupons.txt";

    // BST Node
    private static class Node {

        Coupon coupon;
        Node left, right;

        Node(Coupon coupon) {
            this.coupon = coupon;
        }
    }

    private Node root;

    // HashMap for direct coupon lookup
    private HashMap<String, Coupon> couponMap;

    // Constructor
    public CouponManager() {
        root = null;
        couponMap = new HashMap<>();
    }

    // 1. ADD COUPON
    public boolean addCoupon(String code, String discountType,
                             double discountValue,
                             LocalDate startDate,
                             LocalDate expiryDate,
                             int usageLimit) {

        code = code.trim().toUpperCase(Locale.ROOT);
        discountType = discountType.trim().toUpperCase(Locale.ROOT);

        // Validate coupon details
        if (!isValidCouponData(code, discountType,
                discountValue, startDate, expiryDate, usageLimit)) {

            System.out.println("Invalid coupon details.");
            return false;
        }

        // Check duplicate code
        if (couponMap.containsKey(code)) {
            System.out.println("Duplicate coupon code.");
            return false;
        }

        Coupon coupon = new Coupon(
                code, discountType, discountValue,
                startDate, expiryDate, usageLimit);

        insertIntoBST(coupon);

        couponMap.put(code, coupon);

        return true;
    }

    // 2. INSERT INTO BST
    public void insertIntoBST(Coupon coupon) {
        root = insert(root, coupon);
    }

    private Node insert(Node current, Coupon coupon) {

        if (current == null) {
            return new Node(coupon);
        }

        int compare = coupon.getCode()
                .compareTo(current.coupon.getCode());

        if (compare < 0) {
            current.left = insert(current.left, coupon);
        }
        else if (compare > 0) {
            current.right = insert(current.right, coupon);
        }

        return current;
    }

    // 3. SEARCH COUPON USING HASHMAP
    public Coupon searchCoupon(String code) {

        if (code == null) {
            return null;
        }

        return couponMap.get(
                code.trim().toUpperCase(Locale.ROOT));
    }

    // 4. VALIDATE COUPON
    public boolean validateCoupon(Coupon coupon) {

        return coupon != null
                && coupon.isValidOn(LocalDate.now());
    }

    // 5. APPLY BEST DISCOUNT USING GREEDY
    public DiscountResult applyBestDiscount(double billAmount) {

        if (!Double.isFinite(billAmount) || billAmount <= 0) {
            throw new IllegalArgumentException(
                    "Bill amount must be positive.");
        }

        Coupon bestCoupon = null;
        double bestSaving = 0;

        // Check all available coupons
        for (Coupon coupon : couponMap.values()) {

            if (validateCoupon(coupon)) {

                double saving =
                        coupon.calculateDiscount(billAmount);

                if (saving > bestSaving) {
                    bestSaving = saving;
                    bestCoupon = coupon;
                }
            }
        }

        if (bestCoupon == null) {
            return new DiscountResult(null, 0, billAmount);
        }

        // Reduce usage of selected coupon
        bestCoupon.useCoupon();

        double finalBill = billAmount - bestSaving;

        return new DiscountResult(
                bestCoupon, bestSaving, finalBill);
    }

    // APPLY A SPECIFIC COUPON
    public DiscountResult applyBestDiscount(
            double billAmount, String couponCode) {

        Coupon coupon = searchCoupon(couponCode);

        if (coupon == null) {
            System.out.println(
                    "Coupon " + couponCode + " not found.");

            return new DiscountResult(null, 0, billAmount);
        }

        if (!validateCoupon(coupon)) {
            System.out.println(
                    "Coupon is expired or usage limit reached.");

            return new DiscountResult(null, 0, billAmount);
        }

        double saving = coupon.calculateDiscount(billAmount);

        coupon.useCoupon();

        return new DiscountResult(
                coupon, saving, billAmount - saving);
    }

    // 6. SORT COUPONS
    public List<Coupon> sortCoupons() {

        List<Coupon> sorted =
                new ArrayList<>(couponMap.values());

        Collections.sort(sorted,
                Comparator.comparing(Coupon::getCode));

        return sorted;
    }

    // 7. DISPLAY COUPONS USING BST INORDER TRAVERSAL
    public void displayCoupons() {

        if (root == null) {
            System.out.println("No coupons available.");
            return;
        }

        System.out.println("\n--- COUPON RECORDS ---");

        int[] count = {1};

        displayInOrder(root, count);
    }

    private void displayInOrder(Node current, int[] count) {

        if (current == null) {
            return;
        }

        displayInOrder(current.left, count);

        System.out.println(
                count[0]++ + ". " + current.coupon);

        displayInOrder(current.right, count);
    }

    // 8. GENERATE COUPON REPORT
    public void generateReport() {

        List<Coupon> sorted = sortCoupons();

        if (sorted.isEmpty()) {
            System.out.println("No coupon records available.");
            return;
        }

        System.out.println("\n--- SORTED COUPON REPORT ---");

        for (int i = 0; i < sorted.size(); i++) {
            System.out.println(
                    (i + 1) + ". " + sorted.get(i));
        }
    }

    // 9. SAVE COUPONS INTO FILE
    public void saveCoupons() {

        List<Coupon> records = new ArrayList<>();

        collectInOrder(root, records);

        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(FILE_NAME),
                StandardCharsets.UTF_8)) {

            for (Coupon c : records) {

                writer.write(
                        c.getCode() + "|" +
                        c.getDiscountType() + "|" +
                        c.getDiscountValue() + "|" +
                        c.getStartDate() + "|" +
                        c.getExpiryDate() + "|" +
                        c.getRemainingUses()
                );

                writer.newLine();
            }

            System.out.println(
                    "Coupon records saved successfully.");

        } catch (IOException e) {

            System.out.println(
                    "Error saving file: " + e.getMessage());
        }
    }

    // 10. LOAD COUPONS FROM FILE
    public void loadCoupons() {

        Path path = Paths.get(FILE_NAME);

        if (!Files.exists(path)) {
            System.out.println(
                    "No existing coupon file found.");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split("\\|");

                if (data.length != 6) {
                    continue;
                }

                try {

                    String code = data[0].trim()
                            .toUpperCase(Locale.ROOT);

                    String type = data[1].trim()
                            .toUpperCase(Locale.ROOT);

                    double value =
                            Double.parseDouble(data[2]);

                    LocalDate start =
                            LocalDate.parse(data[3]);

                    LocalDate expiry =
                            LocalDate.parse(data[4]);

                    int uses =
                            Integer.parseInt(data[5]);

                    if (!isValidCouponData(code, type,
                            value, start, expiry, uses)
                            || couponMap.containsKey(code)) {

                        continue;
                    }

                    Coupon coupon = new Coupon(
                            code, type, value,
                            start, expiry, uses);

                    insertIntoBST(coupon);
                    couponMap.put(code, coupon);

                } catch (RuntimeException e) {

                    System.out.println(
                            "Skipping invalid coupon record.");
                }
            }

            System.out.println(
                    "Coupon records loaded successfully.");

        } catch (IOException e) {

            System.out.println(
                    "Error loading file: " + e.getMessage());
        }
    }

    // HELPER: COLLECT BST RECORDS
    private void collectInOrder(
            Node current, List<Coupon> records) {

        if (current == null) {
            return;
        }

        collectInOrder(current.left, records);

        records.add(current.coupon);

        collectInOrder(current.right, records);
    }

    // HELPER: VALIDATE COUPON DETAILS
    private boolean isValidCouponData(
            String code, String type,
            double value, LocalDate start,
            LocalDate expiry, int uses) {

        if (code == null ||
                !code.matches("[A-Z0-9_-]+")) {
            return false;
        }

        if (!type.equals("PERCENTAGE")
                && !type.equals("FLAT")) {
            return false;
        }

        if (!Double.isFinite(value) || value <= 0) {
            return false;
        }

        if (type.equals("PERCENTAGE") && value > 100) {
            return false;
        }

        if (start == null || expiry == null
                || start.isAfter(expiry)) {
            return false;
        }

        return uses > 0;
    }

    // DISCOUNT RESULT CLASS
    public static class DiscountResult {

        private Coupon coupon;
        private double discount;
        private double finalBill;

        public DiscountResult(
                Coupon coupon, double discount,
                double finalBill) {

            this.coupon = coupon;
            this.discount = discount;
            this.finalBill = finalBill;
        }

        public Coupon getCoupon() {
            return coupon;
        }

        public double getDiscount() {
            return discount;
        }

        public double getFinalBill() {
            return finalBill;
        }

        public boolean isCouponApplied() {
            return coupon != null;
        }
    }
}


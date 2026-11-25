
import java.time.LocalDate;
import java.util.Locale;

public class Coupon {

    private String code;
    private String discountType;
    private double discountValue;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private int remainingUses;

    // Constructor
    public Coupon(String code, String discountType,
                  double discountValue, LocalDate startDate,
                  LocalDate expiryDate, int remainingUses) {

        this.code = code.trim().toUpperCase(Locale.ROOT);
        this.discountType = discountType.trim().toUpperCase(Locale.ROOT);
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.remainingUses = remainingUses;
    }

    // Getter methods
    public String getCode() {
        return code;
    }

    public String getDiscountType() {
        return discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public int getRemainingUses() {
        return remainingUses;
    }

    // Reduce coupon usage
    public void useCoupon() {
        if (remainingUses > 0) {
            remainingUses--;
        }
    }

    // Check coupon validity
    public boolean isValidOn(LocalDate date) {

        return !date.isBefore(startDate)
                && !date.isAfter(expiryDate)
                && remainingUses > 0;
    }

    // Calculate discount
    public double calculateDiscount(double billAmount) {

        double discount;

        if (discountType.equals("PERCENTAGE")) {
            discount = billAmount * discountValue / 100;
        } else {
            discount = discountValue;
        }

        // Discount cannot exceed bill amount
        return Math.min(billAmount, discount);
    }

    // Display coupon details
    @Override
    public String toString() {

        String value;

        if (discountType.equals("PERCENTAGE")) {
            value = discountValue + "%";
        } else {
            value = "Flat " + discountValue;
        }

        return code + " | " + value
                + " | Valid: " + startDate + " to " + expiryDate
                + " | Uses left: " + remainingUses;
    }
}


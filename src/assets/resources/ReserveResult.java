package assets.resources;

import universalHelpers.Validation;

public record ReserveResult(Reservation reservation, String failureReason) {
    public ReserveResult {
        if ((reservation == null) == (failureReason == null)) {
            throw new IllegalArgumentException("A result holds either a reservation or a failure reason");
        }
    }

    public static ReserveResult success(Reservation reservation) {
        return new ReserveResult(reservation, null);
    }

    public static ReserveResult failure(String reason) {
        return new ReserveResult(null, Validation.validateString(reason, "Failure reason"));
    }

    public boolean succeeded() {
        return reservation != null;
    }
}
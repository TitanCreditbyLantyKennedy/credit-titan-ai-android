package com.credittitan.ai;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Offline tools. Never predicts scores, eligibility, or legal outcomes. */
public final class CreditTools {
    private CreditTools() {}
    public static final String[] BUREAUS = {"Experian", "Equifax", "TransUnion"};

    public static String utilization(String balance, String limit) {
        try {
            BigDecimal b = new BigDecimal(balance.trim());
            BigDecimal l = new BigDecimal(limit.trim());
            if (b.signum() < 0 || l.signum() <= 0 || b.precision() > 12 || l.precision() > 12
                    || b.scale() > 2 || l.scale() > 2 || b.scale() < 0 || l.scale() < 0) {
                throw new IllegalArgumentException();
            }
            return b.multiply(new BigDecimal("100")).divide(l, 1, RoundingMode.HALF_UP).toPlainString() + "%";
        } catch (RuntimeException invalid) {
            throw new IllegalArgumentException("Enter a balance of zero or more and a limit above zero, using dollars and up to two decimal places.");
        }
    }

    public static String draft(String bureau, String observation) {
        boolean valid = false;
        for (String name : BUREAUS) if (name.equals(bureau)) valid = true;
        if (!valid || observation == null || observation.trim().isEmpty()) {
            throw new IllegalArgumentException("Select a bureau and describe the specific information you believe is inaccurate.");
        }
        return "DRAFT — REVIEW AND COMPLETE BEFORE MAILING\n\n"
                + "Date: [Enter date]\nFrom: [Your name and mailing address]\nTo: " + bureau
                + "\n[Verify the bureau's current dispute mailing address]\n\n"
                + "Subject: Request to investigate information I believe is inaccurate\n\n"
                + "I am requesting an investigation of the following information in my credit report:\n\n"
                + observation.trim() + "\n\n"
                + "Requested correction: [Explain the correction and identify the account with only the details needed.]\n"
                + "Supporting records: [List enclosed copies. Keep your originals.]\n\n"
                + "Please investigate the disputed information and provide the results and an updated report where appropriate.\n\n"
                + "Sincerely,\n[Your name]\n\n"
                + "This draft has not been sent. Review every statement for accuracy, complete the placeholders, "
                + "and check the bureau's submission requirements before using it.";
    }
}

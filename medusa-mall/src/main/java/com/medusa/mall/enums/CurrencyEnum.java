package com.medusa.mall.enums;

/**
 * Currency mapping enum
 */
public enum CurrencyEnum {
    BTC(0, "BTC"),
    USDT(1, "USDT"),
    XMR(2, "XMR"),
    ETH(3, "ETH"),
    LTC(4, "LTC"),
    BCH(5, "BCH"),
    DOGE(7, "DOGE"),
    ADA(8, "ADA"),
    DOT(9, "DOT"),
    LINK(10, "LINK");

    private final int code;
    private final String name;

    CurrencyEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * Get currency code by name (case insensitive)
     * 
     * @param name currency name
     * @return currency code, null if not found
     */
    public static Integer getCodeByName(String name) {
        for (CurrencyEnum currency : CurrencyEnum.values()) {
            if (currency.name.equalsIgnoreCase(name)) {
                return currency.code;
            }
        }
        return null;
    }

    /**
     * Get currency name by code
     * 
     * @param code currency code
     * @return currency name, null if not found
     */
    public static String getNameByCode(int code) {
        for (CurrencyEnum currency : CurrencyEnum.values()) {
            if (currency.code == code) {
                return currency.name;
            }
        }
        return null;
    }

    /**
     * Check if currency name exists
     * 
     * @param name currency name
     * @return true if exists, false otherwise
     */
    public static boolean isValidCurrency(String name) {
        return getCodeByName(name) != null;
    }

    /**
     * Check if currency code exists
     * 
     * @param code currency code
     * @return true if exists, false otherwise
     */
    public static boolean isValidCurrencyCode(int code) {
        return getNameByCode(code) != null;
    }
} 
package com.salesmanager.core.business.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Currency;
import java.util.Locale;

/**
 * Constants used for sm-core (Tinh gọn cho B2C VN)
 */
public class Constants {

  public static final Charset UTF_8 = StandardCharsets.UTF_8;

  public final static String TEST_ENVIRONMENT = "TEST";
  public final static String PRODUCTION_ENVIRONMENT = "PROD";
  public final static String SHOP_URI = "/shop";

  public final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
  public final static String DEFAULT_DATE_FORMAT_YEAR = "yyyy";
  
  // Cấu hình cứng cho thị trường Việt Nam
  public final static String DEFAULT_LANGUAGE = "vi";
  public final static String DEFAULT_COUNTRY = "VN";
  public final static Locale DEFAULT_LOCALE = new Locale("vi", "VN");
  public final static Currency DEFAULT_CURRENCY = Currency.getInstance(new Locale("vi", "VN"));

  public final static String EMAIL_CONFIG = "EMAIL_CONFIG";

  public final static String UNDERSCORE = "_";
  public final static String SLASH = "/";
  public final static String TRUE = "true";
  public final static String FALSE = "false";
  
  // Các hằng số dùng cho việc tính toán Đơn hàng (Giỏ hàng)
  public final static String OT_ITEM_PRICE_MODULE_CODE = "itemprice";
  public final static String OT_SUBTOTAL_MODULE_CODE = "subtotal";
  public final static String OT_TOTAL_MODULE_CODE = "total";
  public final static String OT_SHIPPING_MODULE_CODE = "shipping";
  public final static String OT_DISCOUNT_TITLE = "order.total.discount";

  public final static String PAYMENT_MODULES = "PAYMENT";
}
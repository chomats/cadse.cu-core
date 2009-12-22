package fr.imag.adele.cadse.core.impl.attribute;


public class Messages extends NLS {
	private static final String BUNDLE_NAME = "fr.imag.adele.cadse.core.impl.attribute.messages"; //$NON-NLS-1$
	public static String bad_type;
	public static String cannot_be_empty;
	public static String cannot_be_undefined;
	public static String cannot_connvert_to_enum_clazz;
	public static String cannot_convert_to_from;
	public static String cannot_convert_to_int;
	public static String cannot_convert_to_long;
	public static String must_be_a_boolean;
	public static String must_be_a_double;
	public static String must_be_a_long;
	public static String must_be_a_string;
	public static String must_be_an_integer;
	public static String unkown_value;
	public static String value_must_be_upper;
	public static String value_must_be_lower;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}

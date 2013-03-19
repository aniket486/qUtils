


public class CheckNumber {

	boolean checkNumber(String s) {
		s = s.replace(",", "").trim();
		if(s.startsWith("-")) {
			s = s.substring(1);
		}
		try {
			double d = Double.parseDouble(s);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
		CheckNumber cn = new CheckNumber();
		System.out.println(cn.checkNumber("-123,12.123"));
		System.out.println(cn.checkNumber("-1231212321312"));
	}
	
}

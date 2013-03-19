
public class CellPhoneDialer {
	String[] keyboardCodes = { 
			"0",
			"1",
			"abc",
			"def",
			"ghi",
			"jkl",
			"mno",
			"pqrs",
			"tuv",
			"wxyz"
	};
	
	void printAllCodes(String phNumber) {
		printCode("", phNumber.replace("-", ""));
	}
	
	void printCode(String prefixCode, String number) {
		String currentCodes = getKeyboardCode(number.charAt(0));
		String newPrefix;
		if(currentCodes.length()==0) {
			newPrefix = prefixCode + " ";
			if(number.length() == 1) {
				System.out.println(newPrefix);
			} else {
				printCode(newPrefix, number.substring(1));
			}
		}
		for(int i=0; i< currentCodes.length(); i++) {
			newPrefix = prefixCode + currentCodes.charAt(i);
			if(number.length() == 1) {
				System.out.println(newPrefix);
			} else {
				printCode(newPrefix, number.substring(1));
			}
		}
	}
	
	String getKeyboardCode(char num) {
		int val = Character.getNumericValue(num);
		if(val == -1) {
			throw new RuntimeException();
		}
		return keyboardCodes[val];
	}
	
	public static void main(String[] args) {
		CellPhoneDialer cpd = new CellPhoneDialer();
		cpd.printAllCodes("412-999-0661");
	}
}

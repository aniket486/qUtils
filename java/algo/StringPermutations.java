
public class StringPermutations {

	static void permute(String prefix, String suffix) {
		if(suffix.length()==0) {
			System.out.println(prefix+suffix);
			return;
		}
		
		for(int i =0; i < suffix.length(); i++) {
			permute(prefix+suffix.charAt(i), suffix.substring(0,i)+suffix.substring(i+1));
		}
		
	}
	
	public static void main(String[] args) {
		String s= "aniket";
		permute("",s);		
	}
}


public class PermuteStrings {
	void perm1(String s) {
		perm1("",s);
	}
	
	void perm1(String fix, String var) {
		int len = var.length();
		if(len == 1) {
			System.out.println(fix+var);
			return;
		}
		for(int i=0; i<len; i++) {
			perm1(fix+var.charAt(i), var.substring(0,i)+var.substring(i+1));
		}
	}
	
	void perm2(String s) {
		perm2(s,0);
	}
	
	void perm2(String s, int n) {
		if(n==s.length()-1) {
			System.out.println(s);
			return;
		}
		for(int i = n; i < s.length(); i++) {
			String t = swap(s, n, i);
			perm2(t, n+1);
		}
	}
	
	/* n < i*/
	String swap(String s, int n, int i) {
		if(i==s.length()-1) {
			return s.charAt(i)+s.substring(n+1,i)+s.charAt(n);
		} else {
			return s.charAt(i)+s.substring(n+1,i)+s.charAt(n) + s.substring(i+1);
		}
		
	}
	
	void perm3(String s) {
		perm3(s, s.length());
	}
	
	void perm3(String s, int len) {
		
	}
	
	public static void main(String[] args) {
		PermuteStrings ps = new PermuteStrings();
		//ps.perm1("org");
		ps.perm2("org");
	}
}

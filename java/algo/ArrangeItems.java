
public class ArrangeItems {
	void ArrangeOnesAndZeros(int[] arr) {
		int zInd = 0;
		int oInd = arr.length -1;
		while(zInd < oInd) {
			zInd = findZInd(zInd, arr);
			oInd = findOInd(oInd, arr);
			swap(arr, zInd, oInd);
		}
		swap(arr,zInd, oInd);
	}
	
	void ArrangeZerosOnesandTwos(int[] arr) {
		int zInd = 0;
		int oInd = 0;
		int tInd = arr.length-1;
		
		while(oInd <= tInd) {
			if(arr[oInd] == 0) {
				if(zInd<oInd) {
					swap(arr, zInd, oInd);
					zInd++;
				} else {
					oInd++;
					zInd++;
				}
			} else if(arr[oInd] == 2) {
				if(tInd>oInd) {
					swap(arr, tInd, oInd);
					tInd--;
				} else {
					oInd++;
					tInd--;
				}
			} else {
				oInd++;
			}
		}
	}
	
	private int findZInd(int startInd, int[] arr) {
		int i=startInd;
		while(arr[i]==0) {
			i++;
		}
		return i;
	}
	
	private int findOInd(int endInd, int[] arr) {
		int i=endInd;
		while(arr[i]==1) {
			i--;
		}
		return i;
	}
	
	private void swap(int[] arr, int ind1, int ind2) {
		int temp = arr[ind2];
		arr[ind2] = arr[ind1];
		arr[ind1] = temp;
	}
	
	private void printArray(int[] arr) {
		for(int ele:arr) {
			System.out.print(ele + " ");
		}
	}
	
	public static void main(String[] args) {
		ArrangeItems arrItems = new ArrangeItems();
		//int arr[] = {1,0,1,0,1,1,1,0,1,0,1,1,0,0,0,1,0,0,1};
		int arr[] = {1,1,1,1,1,1,1,0,0,0,0,0,0,0};
		int arr2[] = {1,0,2,0,1,0,1,2,1,0,1,1,2,0,0,1,0,0,1,2,1,2,1,2,0,1,2,0,1,2};
		//arrItems.ArrangeOnesAndZeros(arr);
		arrItems.ArrangeZerosOnesandTwos(arr);
		arrItems.printArray(arr);
		System.out.println();
		arrItems.printArray(arr);
	}
}

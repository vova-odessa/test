package nntest2.data;

public class IndexData extends Data {
	public static final String prefix = "(@ind=";
	public int index = -1;
	
	/**
	 * constructor for direct construction
	 * @param index
	 */
	public IndexData(int index) {
		this.index = index;
	}
	
	/**
	 * constructor for restoring from string construction
	 * @param index
	 */
	public IndexData(String indexData) throws Exception{
		if(indexData.startsWith(prefix)) {
			String indStr = indexData.substring(prefix.length(), indexData.length() - 1);
			
			try {
				index = Integer.parseInt(indStr);
			} catch(Throwable t) {				
			}
		}
		
		throw new Exception("IndexData: input is not IndexData - " + indexData);
	}
	
	@Override
	public String toString() {
		return prefix + index + ")";
	}
}

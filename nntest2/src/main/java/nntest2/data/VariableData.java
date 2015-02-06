package nntest2.data;

public class VariableData extends Data {
	String variableName = null;
	private static final String VAR_PREFIX = "@";
	
	public VariableData(String data) throws Exception{
		data = data.trim();
		if(!data.startsWith(VAR_PREFIX) || hasSpecial(data)) {
			throw new Exception();
		}
		
		variableName = data.substring(VAR_PREFIX.length());
	}

	private static boolean hasSpecial(String data) {
		return data.matches("@\\w*\\W.*");
	}
	
	@Override
	public String toString() {
		return VAR_PREFIX + variableName;
	}
	
	/*
	 * test for variable parsing
	 * public static void main(String[] args) {
		System.out.println(regex);
		try {
			VariableData var = new VariableData("@hhh");
		} catch (Exception e) {
			System.out.println("1");
		}
		
		try {
			VariableData var = new VariableData("@hhh ");
		} catch (Exception e) {
			System.out.println("2");
		}
		
		try {
			VariableData var = new VariableData("@hhh	d");
		} catch (Exception e) {
			System.out.println("3");
		}
		
		try {
			VariableData var = new VariableData("@hhh\nddd");
		} catch (Exception e) {
			System.out.println("4");
		}
	}*/
	
}

package org.droidplanner.helpers;

import android.content.Context;

public class ValueKey {

	public static class ValueKeyData{
		public int[] values;
		public String[] keys;
	}

	
	private Context context;
	private int resourceID;
	
	public ValueKey(Context context, int resourceID){
		this.context = context;
		this.resourceID = resourceID;
		if(this.context!=null && resourceID!=0)
			getOptions(this.context,this.resourceID);
		
	}
	
	public static ValueKeyData getOptions(Context context, int resourceID) {
		String pairs[] = context.getResources().getStringArray(resourceID);
		ValueKeyData data = new ValueKeyData();
		
		if(pairs==null)
			return null;
		
		data.values = null;
		data.values = new int[pairs.length];
		data.keys = null;
		data.keys = new String[pairs.length];

		int i = 0;
		for (String item : pairs) {
			String pair[] = item.split(";");
			data.values[i] = Integer.parseInt(pair[0]);
			data.keys[i] = pair[1];
			i++;
		}
		return data;
	}

}

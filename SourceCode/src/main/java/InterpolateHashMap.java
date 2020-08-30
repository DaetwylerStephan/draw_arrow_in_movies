import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class InterpolateHashMap {

	
	private static int[] fillupzerovalues(int[] values){
		int[] newvalues = values.clone();
		
		for(int i =0;i<values.length;i++){
			
			if(newvalues[i]==0){//you have to fill it up if it is zero
			int valuebefore = newvalues[i-1];
			int nextvalue = 0;
			int nextindex =0;
			int searchiter=1;
			while(nextvalue ==0){
				nextvalue = values[i+searchiter];
				searchiter++;
				nextindex = i+searchiter;
			}
			
			double slope = (double) (nextvalue - valuebefore)/((double) (nextindex - (i)));
			
			//fill up all non-zero values linearly
			for(int currentiter=i;currentiter<nextindex;currentiter++) {
				newvalues[currentiter]= (int) (valuebefore + (currentiter-i+1)*slope/1);						
			}
			i=nextindex-1;//skip to the next non-zero value - (-1) because value will be increased by i++
			}
		}	
		return newvalues;
	}
	
	public int[][] interpolateList(HashMap startmap){
		
	//---------------------average start map to have one point per frame
	
	HashMap<Integer, ArrayList<int[]>> medianpointmap = new HashMap<Integer, ArrayList<int[]>>();
	
    Set set = startmap.entrySet();
    Iterator iterator = set.iterator();
        
    	while(iterator.hasNext()) {
           Map.Entry mentry = (Map.Entry)iterator.next();
           ArrayList<int[]> tmp = (ArrayList<int[]>) mentry.getValue();
           
           int[] valuex = new int[tmp.size()];
           int[] valuey = new int[tmp.size()];

           for(int i=0;i<tmp.size();i++){
        	   valuex[i] = tmp.get(i)[0];
        	   valuey[i] = tmp.get(i)[1];
        	}
           Arrays.sort(valuex);Arrays.sort(valuey);
           
           //calculate the median of all points selected here
           int medianx; int mediany;
           if (valuex.length % 2 == 0){
               medianx = (int) Math.round(((double)valuex[valuex.length/2] + (double)valuex[valuex.length/2 - 1])/2);
               mediany = (int) Math.round(((double)valuey[valuey.length/2] + (double)valuey[valuey.length/2 - 1])/2);
           }
           else{
               medianx = (int) valuex[valuex.length/2];
               mediany = (int) valuey[valuey.length/2];
           }
           int[] median = {medianx,mediany};
           
           ArrayList<int[]> currenttmp = new ArrayList<int[]>();
           currenttmp.add(median);
           
           medianpointmap.put((Integer) mentry.getKey(), new ArrayList<int[]>());
           medianpointmap.put((Integer) mentry.getKey(), currenttmp);  
        }	
    //System.out.println("map after averaging> ");	
    //printhashmap(medianpointmap);
	//--sort-----------------------------------------------------------------------------------------------------------------
    
	Map<Integer, ArrayList<int[]>> mediansortedmap = new TreeMap<Integer, ArrayList<int[]>>(medianpointmap);//order HashMap based on keys. 
	
	/////-----------translate to three arrays for better interpolation-------------------------------------------------------
	int nbkeys = mediansortedmap.size();

	int[] planenbarray = new int[nbkeys];
	int[] posxarray = new int[nbkeys];
	int[] posyarray = new int[nbkeys];
	
	Set mediansortedset = mediansortedmap.entrySet();
    Iterator iteratormediansortedset = mediansortedset.iterator();

    int i = 0;
    while(iteratormediansortedset.hasNext()) {
    	Map.Entry mentry = (Map.Entry)iteratormediansortedset.next();
    	planenbarray[i]	= (Integer) mentry.getKey(); 	
    	ArrayList<int[]> tmp = (ArrayList<int[]>) mentry.getValue();
    	posxarray[i] = tmp.get(0)[0];
    	posyarray[i] = tmp.get(0)[1];
    	i++;
	}
    
    /////-----------interpolate-------------------------------------------------------
	
	///fill up array with known values--------------
	int startindex = planenbarray[0];
	int endindex = planenbarray[planenbarray.length-1];
	int range = endindex - startindex+1;
			
	int[] newplanenb = new int[range];
	int[] newpositionsx = new int[range];
	int[] newpositionsy = new int[range];
		    
		   
	for(int iter =0;iter<newplanenb.length;iter++){
		int currentiter = iter + startindex;
		newplanenb[iter]=currentiter;
		    
		for(int ii =0;ii<planenbarray.length;ii++){
		    	if(planenbarray[ii]==currentiter){
		    		newpositionsx[iter]=posxarray[ii];
		    		newpositionsy[iter]=posyarray[ii];
		    	}	
		  }
	}    
		    
	//------replace zero values (linear interpolation) -------------------
	newpositionsx = fillupzerovalues(newpositionsx);
	newpositionsy=fillupzerovalues(newpositionsy);
    	 
	int[][] combinevalues =  {newpositionsx,newpositionsy, newplanenb};
		    
	return combinevalues;		
	}
	
//////////////////-------------------------------main to test
	public static void main(String[] args2) {

		///test
		int[] planenbarray = {1,5,10};
		int[] posxarray = {1,101,133};
		int[] posyarray = {2,31,500};	

		///fill up array with known values--------------
		int startindext = planenbarray[0];
		int endindext = planenbarray[planenbarray.length-1];
		int ranget = endindext - startindext+1;

		int[] newplanenb = new int[ranget];
		int[] newpositionsx = new int[ranget];
		int[] newpositionsy = new int[ranget];

		for(int iter =0;iter<newplanenb.length;iter++){
			int currentiter = iter + startindext;
			newplanenb[iter]=currentiter;

			for(int ii =0;ii<planenbarray.length;ii++){
				if(planenbarray[ii]==currentiter){
					newpositionsx[iter]=posxarray[ii];
					newpositionsy[iter]=posyarray[ii];
				}	
			}
		}

		System.out.println(Arrays.toString(newplanenb));
		System.out.println(Arrays.toString(newpositionsx));
		System.out.println(Arrays.toString(newpositionsy));

		//	------replace zero values-------------------
		newpositionsx = fillupzerovalues(newpositionsx);
		newpositionsy=fillupzerovalues(newpositionsy);

		System.out.println(Arrays.toString(newpositionsx));
		System.out.println(Arrays.toString(newpositionsy));

	}
	///////////////-------------------------------------end main to test

	
	
	private void printhashmap(HashMap pointmap){
		 Set set = pointmap.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
           Map.Entry mentry = (Map.Entry)iterator.next();
           System.out.print("key is: "+ mentry.getKey() + " & Value is: ");
           ArrayList<int[]> tmp = (ArrayList<int[]>) mentry.getValue();
           for(int i=0;i<tmp.size();i++){
           System.out.println(Arrays.toString(tmp.get(i)) + " ");
        }
        }
	}
	
	
}

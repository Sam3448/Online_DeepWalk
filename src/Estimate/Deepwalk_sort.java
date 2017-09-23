package Estimate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class Deepwalk_sort {
	double W[][];
	int dataNum;
	int dimension=200;
	public Deepwalk_sort  (File f_group) throws Exception{
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(f_group)));
		String line = "";
		int countLine=0;
		while((line = trUsers.readLine())!=null){
				countLine++;
		}
		trUsers.close();
		this.dataNum=countLine;
		this.W=new double[dataNum][dimension];
	}
	public void loadEmbedding(File f_W) throws Exception, IOException{
		String temp;
		BufferedReader br = new BufferedReader(
	            new InputStreamReader(new FileInputStream(f_W)));
		while ((temp = br.readLine()) != null){
			String[] strs = temp.split(" ");
			int index=Integer.valueOf(strs[0]);
			for(int i=0;i<dimension;i++)
					W[index][i]=Double.parseDouble(strs[i+1]);
		}

	}
	public void sortEmbedding(File f_print) throws Exception{
		FileWriter fw=new FileWriter(f_print);
		for(int i=0;i<dimension;i++){
			for(int j=0;j<dataNum;j++){
				fw.write(String.valueOf(W[j][i])+" ");
			}
			fw.write('\n');
		}
		fw.close();
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String source="wiki";
		Deepwalk_sort dws=new Deepwalk_sort(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/group/group_"+source+".txt"));
		dws.loadEmbedding(new File("/Users/zwc34/Documents/Result/W_"+source+".txt"));
		dws.sortEmbedding(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/W_DW/W_"+source+"_LINE.txt"));
	}

}

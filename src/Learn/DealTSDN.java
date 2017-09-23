package Learn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class DealTSDN {
	int dataNum;
	int dimension=200;
	double W[][];
	int TestTrain[];
	File group;
	double limitRandom=0.5;
	public DealTSDN  (File f_group) throws Exception{
		this.group=f_group;
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(f_group)));
		String line = "";
		int countLine=0;
		while((line = trUsers.readLine())!=null){
				countLine++;
		}
		trUsers.close();
		this.dataNum=countLine;
		this.W=new double[dataNum][dimension];
		this.TestTrain=new int[dataNum];
	}
	
	/*public void makeTrainTest() throws Exception{
		int labels[]=new int[dataNum];
		int labelClassNum[]=new int[20];
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(group)));
		String line = "";
		while((line = trUsers.readLine())!=null){
				String[] strs = line.split("\t");
				labels[Integer.valueOf(strs[0])]=Integer.valueOf(strs[1]);
				labelClassNum[labels[Integer.valueOf(strs[0])]]++;
		}
		trUsers.close();
		int countClassNum=0;
		for(int i=0;i<labelClassNum.length;i++){
			if(labelClassNum[i]!=0)
				countClassNum++;
		}
		int labelClass[]=new int[countClassNum];
		Random r_classifier=new Random(123l);
		for(int i=0;i<dataNum;i++){
			double nextRandom=r_classifier.nextDouble();
			if(nextRandom<=limitRandom){
				TestTrain[i]=1; //train
				labelClass[labels[i]]++;
			}
		}
		for(int i=0;i<labelClass.length;i++){
			if(labelClass[i]==0){
				Random r_classifier2=new Random(123l);
				for(int j=0;j<dataNum;j++){
					if(labels[j]==i){
						double nextRandom2=r_classifier2.nextDouble();
						if(nextRandom2<=limitRandom){
							TestTrain[j]=1;//train
						}
					}
				}
			}
		}
		
	}*/
	public void makeTrainTest() throws Exception, IOException{
		int labels[]=new int[dataNum];
		int labelClassNum[]=new int[20];
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(group)));
		String line = "";
		while((line = trUsers.readLine())!=null){
				String[] strs = line.split("\t");
				labels[Integer.valueOf(strs[0])]=Integer.valueOf(strs[1]);
				labelClassNum[labels[Integer.valueOf(strs[0])]]++;
		}
		trUsers.close();
		int countClassNum=0;
		for(int i=0;i<labelClassNum.length;i++){
			if(labelClassNum[i]!=0)
				countClassNum++;
		}
		int labelClass[]=new int[countClassNum];
		int count1=0;
		for(int i=0;i<dataNum;i++){
			if(labelClass[labels[i]]<50){
				count1++;
				TestTrain[i]=1;
				labelClass[labels[i]]++;
			}
		}
		System.err.println(count1+"  classNum = "+countClassNum);
	}
	
	public void readAndWriterLabel(File writeLabel) throws Exception{
		int labels[]=new int[dataNum];
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(group)));
		String line = "";
		while((line = trUsers.readLine())!=null){
				String[] strs = line.split("\t");
				labels[Integer.valueOf(strs[0])]=Integer.valueOf(strs[1]);
		}
		trUsers.close();
		FileWriter fw=new FileWriter(writeLabel);
		for(int i=0;i<labels.length;i++){
			if(TestTrain[i]==1)
				fw.write(String.valueOf(labels[i])+'\n');
		}
		fw.close();
	}
	public void writeDeepwalkEmbedding(File dwembedding, File outembedding)throws Exception, IOException{
    	String temp212;
    	int rowNum=0;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dwembedding)));
		while ((temp212 = br.readLine()) != null){
			String[] strs = temp212.split(" ");
			for(int i=0;i<strs.length;i++){
				W[i][rowNum]=Double.parseDouble(strs[i]);
			}
			rowNum++;
		}
		br.close();
		FileWriter fw=new FileWriter(outembedding);
		for(int i=0;i<dataNum;i++){
			if(TestTrain[i]==1){
				for(int j=0;j<dimension;j++)
					fw.write(String.valueOf(W[i][j])+" ");
				fw.write('\n');
			}
		}
		fw.close();
	}
	public void writeMMDWEmbedding(File MMDWembedding, File outMMDWembedding)throws Exception, IOException{
		String temp;
		int countLine=0;
		double vector[][]=new double[dataNum][dimension];
		BufferedReader br = new BufferedReader(
	            new InputStreamReader(new FileInputStream(MMDWembedding)));
		while ((temp = br.readLine()) != null){
			String[] strs = temp.split(" ");
				for(int i=0;i<dimension;i++)
					vector[countLine][i]=Double.parseDouble(strs[i]);
				countLine++;
		}
		br.close();
		FileWriter fw=new FileWriter(outMMDWembedding);
		for(int i=0;i<dataNum;i++){
			if(TestTrain[i]==1){
				for(int j=0;j<dimension;j++)
					fw.write(String.valueOf(vector[i][j])+" ");
				fw.write('\n');
			}
		}
		fw.close();
	
	}

	public static void main(String[] args) throws Exception {
		String source="wiki";
		DealTSDN ds=new DealTSDN(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/group/group_"+source+".txt"));
		ds.makeTrainTest();
		ds.readAndWriterLabel(new File("/Users/zwc34/Documents/T-SNE/all_top50/"+source+"/label.txt"));
		ds.writeDeepwalkEmbedding(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/W_DW/W_"+source+"_deepwalk.txt"),
				new File("/Users/zwc34/Documents/T-SNE/all_top50/"+source+"/deepwalk.txt"));
		ds.writeMMDWEmbedding(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/vector/"+source+"_alphaBiasLevel_-2_vectorall_7->80.0%.txt"),
				new File("/Users/zwc34/Documents/T-SNE/all_top50/"+source+"/MMDW.txt"));
	}

}

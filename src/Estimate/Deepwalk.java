package Estimate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import Learn.StoreAlphaWeight;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class Deepwalk {
	static double limitRandom;
	File labelFile;
	static String rootFolder;
	static String source;
	int TestTrain[];
	int classNumber;
	
	int dataNum;
	int dimension=200;
	int labels[];
	double W[][];
	
	int test;
	int train;
	Feature vectrain[][];
	Feature vectest[][];
	double trainattr[];
	double testattr[];
	
	public Deepwalk(File label,File embedding) throws Exception{
		countLine(label);
		loadEmbedding(embedding);//读入W
		makeTrainTest();//读入Label，做出TestTrain
		countTestTrain();
		prepTrainData();
		prepTestData();
	}
	public void countLine  (File f_group) throws Exception{
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(f_group)));
		String line = "";
		int countLine=0;
		while((line = trUsers.readLine())!=null){
				countLine++;
		}
		trUsers.close();
		this.labelFile=f_group;
		this.dataNum=countLine;
		this.TestTrain=new int[dataNum];
		this.labels=new int[dataNum];
		this.W=new double[dataNum][dimension];
	}
	public void loadEmbedding(File f_W) throws Exception, IOException{
		/*String temp;
		BufferedReader br = new BufferedReader(
	            new InputStreamReader(new FileInputStream(f_W)));
		while ((temp = br.readLine()) != null){
			String[] strs = temp.split(" ");
			int index=Integer.valueOf(strs[0]);
			for(int i=0;i<200;i++)
					W[index][i]=Double.parseDouble(strs[i+1]);
		}*/
		String temp;
		int countLine=0;
		BufferedReader br = new BufferedReader(
	            new InputStreamReader(new FileInputStream(f_W)));
		while ((temp = br.readLine()) != null){
			String[] strs = temp.split(" ");
				for(int i=0;i<200;i++)
					W[countLine][i]=Double.parseDouble(strs[i]);
				countLine++;
		}
		System.out.println("countLine = "+countLine);
	
	}
	public void makeTrainTest() throws Exception{
		int labelClassNum[]=new int[20];
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(labelFile)));
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
		this.classNumber=countClassNum;
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
		
	}
	public void countTestTrain(){
		int countTest=0,countTrain=0;
		for(int i=0;i<dataNum;i++){
			if(TestTrain[i]==1){
				countTrain++;
			}
			else{
				countTest++;
			}
		}
		this.test=countTest;
		this.train=countTrain;
		vectrain=new Feature[train][dimension];
		vectest	=new Feature[test][dimension];
		trainattr=new double[train];
		testattr =new double[test];
		System.out.println("test + train = "+(test+train));
	}
	public void prepTrainData() throws Exception{
		int index=0;
		for(int i=0;i<dataNum;i++){
			if(TestTrain[i]==0)//test
				continue;
			else{
			for(int j=0;j<dimension;j++){
				this.vectrain[index][j]=new FeatureNode(j+1,W[i][j]);
			}
			this.trainattr[index]=labels[i];
			index++;
			}
		}
	}
	public void prepTestData(){
		int index=0;
		for(int i=0;i<dataNum;i++){
			if(TestTrain[i]==1)
				continue;
			else{
			for(int j=0;j<dimension;j++){
				this.vectest[index][j]=new FeatureNode(j+1,W[i][j]);
			}
			this.testattr[index]=labels[i];
			index++;
			}
		}
	}
	public void lib(){
		int right=0;
		Problem problemg=new Problem();
		problemg.l=train; //为训练样本数目
		problemg.n=dimension;
		problemg.x=vectrain;//为训练样本向量值
		problemg.y=trainattr;//为训练样本的类别数值
		SolverType s=SolverType.MCSVM_CS;  //MCSVM
        double C = 10;    // cost of constraints violation
        double eps = 0.1; // stopping criteria
        Parameter parameter = new Parameter(s, C, eps);
		StoreAlphaWeight saw=new StoreAlphaWeight();
		saw.alphaB=new double[dataNum][classNumber];
		saw.weightB=new double[classNumber][dimension];
		saw.dimensionForSVM=dimension;
        Model modelg = Linear.train(problemg, parameter, saw);
        File mg = new File(rootFolder+"svm_model/modelDeepwalk.txt");
        try {
			modelg.save(mg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // load model or use it directly
        try {
			modelg = Model.load(mg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        for(int t=0;t<test;t++){
            double prediction = Linear.predict(modelg, vectest[t]);
            //System.out.println(testattr[t]+"\t"+prediction);
            if(prediction==testattr[t]){
            	right++;
            }
          }
        double precision=(double)right/test;
        System.out.println("right= "+right+"  test= "+test);
        System.out.println("precision rate = "+precision*100+"%");
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
			Deepwalk.limitRandom=0.1;
			Deepwalk.source="citeseer";
			Deepwalk.rootFolder="/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/";
			File labeld=new File(rootFolder+"group/group_"+source+".txt");
			File embedding=new File(rootFolder+"W_DW/W_"+source+".txt");
			Deepwalk dw=new Deepwalk(labeld, embedding);
			dw.lib();
	}

}

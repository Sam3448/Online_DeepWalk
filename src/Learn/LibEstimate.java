package Learn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LibEstimate  {
	int zhe=2;
	int data;
	double vector[][];
	double bias[][];
	int attr[];
	int test;
	int train;
	double lambda;
	Feature vectrain[][];
	Feature vectest[][];
	double trainattr[];
	double testattr[];
	public void countLine() throws Exception{
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentationEmotion_v3/data/group/group_cora.txt"))));
		String line = "";
		int countLine=0;
		while((line = trUsers.readLine())!=null){
				countLine++;
		}
		this.data=countLine;
		trUsers.close();
		test=(int)Math.ceil(data/zhe);
		train=data-test;
		vectrain=new Feature[train][200];
		vectest=new Feature[test][200];
		trainattr=new double[train];
		testattr=new double[test];
		vector=new double[data][200];
		bias=new double[data][200];
		attr=new int[data];

	}
	public void readVec() throws Exception{
		String temp;
		int countLine=0;
		File f=new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentationEmotion_v3/data/Embedding/cora_vec_all.txt");
		BufferedReader br = new BufferedReader(
	            new InputStreamReader(new FileInputStream(f)));
		while ((temp = br.readLine()) != null){
			String[] strs = temp.split(" ");
				for(int i=0;i<200;i++)
					vector[countLine][i]=Double.parseDouble(strs[i]);
				countLine++;
		}
		System.out.println("countLine = "+countLine);
	}
	public void readLabel() throws Exception{
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentationEmotion_v3/data/group/group_cora.txt")));
		String line = "";
		String strs[];
		int countline=0;
		while((line = trUsers.readLine())!=null){
					strs=line.split("\t");
					attr[countline]=Integer.valueOf(strs[1]);
					countline++;
				
		}
		trUsers.close();
	}
	public void readBias() throws Exception{
		String temp;
		int countLine=0;
		File f = new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentationEmotion_v3/data/Bias/citeseer_alphaBiasLevel_-2_Bias_4->50.0%.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		while ((temp = br.readLine()) != null){
			String[] strs = temp.split("\t");
			for(int i=0;i<200;i++)
				bias[countLine][i]=Double.parseDouble(strs[i+2]);
			countLine++;
		}
		System.out.println("countLine = "+countLine);
	}
	public void addBiastoVec() throws Exception{
		lambda=0;
		for(int i=0;i<data;i++){
			for(int j=0;j<200;j++)
				vector[i][j]+=lambda*bias[i][j];
		}
		FileWriter fw=new FileWriter(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentationEmotion_v3/data/vector/deepwalk_after.txt"));
		for(int i=0;i<data;i++){
			for(int j=0;j<200;j++){
				fw.write(String.valueOf(vector[i][j])+" ");
			}
			fw.write('\n');
		}
		fw.close();
	}
	public void prepTrainData(){
		//System.out.println("train= "+train);
		int index=0;
		int countZero=0;
		for(int i=0;i<data;i++){
			if(i%zhe==zhe-1)
				continue;
			else{
			double sum = 0;
			for(int j=0;j<200;j++){
				this.vectrain[index][j]=new FeatureNode(j+1,vector[i][j]);
				sum += vector[i][j]*vector[i][j];
			}
			if(sum == 0){
				countZero++;
			}
			this.trainattr[index]=attr[i];
			index++;
			}		
		}
		System.out.println("countZero= "+countZero);
	}
	public void prepTestData(){
		int index=0;
		int countZero=0;
		for(int i=0;i<data;i++){
			if(i%zhe!=zhe-1)
				continue;
			else{
			double sum = 0;
			for(int j=0;j<200;j++){
				this.vectest[index][j]=new FeatureNode(j+1,vector[i][j]);
				sum += vector[i][j]*vector[i][j];
			}
			if(sum == 0){
				countZero++;
			}
			this.testattr[index]=attr[i];
			index++;
			}		
		}
		System.out.println("countZero= "+countZero);
	}
	public void lib(){
		int right=0;
		Problem problemg=new Problem();
		problemg.l=train; //为训练样本数目
		problemg.n=200;
		problemg.x=vectrain;//为训练样本向量值
		problemg.y=trainattr;//为训练样本的类别数值
		SolverType s=SolverType.MCSVM_CS;  //MCSVM
        double C = 10;    // cost of constraints violation
        double eps = 0.1; // stopping criteria
        Parameter parameter = new Parameter(s, C, eps);
		StoreAlphaWeight saw=new StoreAlphaWeight();
        Model modelg = Linear.train(problemg, parameter, saw);
        File mg = new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentationEmotion_v3/data/svm_model/modelLib.txt");
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
		StoreAlphaWeight.dimensionForSVM=200;
		LibEstimate le=new LibEstimate();
		le.countLine();
		le.readVec();
		le.readLabel();
		le.readBias();
		le.addBiastoVec();
		le.prepTrainData();
		le.prepTestData();
		le.lib();
	}
	

}



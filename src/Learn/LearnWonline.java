package Learn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import Neuron.HiddenNeuron;

import VecUtil.Haffman;

import Neuron.Neuron;

import Neuron.WordNeuron;

import Learn.Evaluate_SVM;


public class LearnWonline extends Thread {
	double lambda;
	int flagNum;
	double alpha;
	double alphaBias;
	double limitRandom;
	boolean init=true;
	static String modelFile;
	static String sequenceFile;
	static String labelFile;
	static String W_DW;
	static String source;
	boolean writeVec=true;
	static int alphaLevel;
	
	static int dataNum;//3312;//2708;
	static int dimension=200;//fixed
	int steps_init=40;//fixed
	int steps_after=40;
	int trainWordsCount=0;
	static boolean test_switch;
	StoreAlphaWeight saw;
	
	Map<String, Neuron> wordMap = new HashMap<String, Neuron>();
	int EXP_TABLE_SIZE=1000;
	static int LOOP=1;
	int MAX_EXP = 6;
	double expTable[] = new double[EXP_TABLE_SIZE];
	double startingAlpha;
	double sample=1e-4;
	static boolean isCbow;
	int window=5;
	
	static boolean NEG;
	static boolean hs;
	static int negSample=10;
	int table_size=1000000;
	int table[]=new int[table_size];
	
	int countLoopTime;
	
	double W[][];
	double bias[][];
	int TestTrain[];
	public LearnWonline()throws Exception{
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(labelFile)));
		int countDataNum=0;
		String line="";
		while((line = trUsers.readLine())!=null){
			countDataNum++;
		}
		trUsers.close();
		dataNum=countDataNum;
		W=new double[dataNum][dimension];
		bias=new double[dataNum][dimension];
		TestTrain=new int[dataNum];
		createExpTable();
	}
    private void createExpTable() {
        for (int i = 0; i < EXP_TABLE_SIZE; i++) {
            expTable[i] = Math.exp(((i / (double) EXP_TABLE_SIZE * 2 - 1) * MAX_EXP));
            expTable[i] = expTable[i] / (expTable[i] + 1);
        }
    }
	
	public void makeGraph(File sequenceFile) throws Exception{
		readVocab(sequenceFile);
        new Haffman(dimension).make(wordMap.values()); 
        //查找每个神经元
        for (Neuron neuron : wordMap.values()) {
            ((WordNeuron)neuron).makeNeurons() ;
        }
        InitUnigramTable();
        trainW(sequenceFile);
        init = false;
	}
	public void readVocab(File sequenceFile) throws Exception{
        MapCount<String> mc = new MapCount<String>();
        BufferedReader br = new BufferedReader(
            new InputStreamReader(new FileInputStream(sequenceFile))); {
            String temp = null;
            while ((temp = br.readLine()) != null) {
                String[] split = temp.split(" ");
                trainWordsCount += split.length;
                for (String string : split) {
                    mc.add(string);
                }
            }
        }
        for (Entry<String, Integer> element : mc.get().entrySet()) {
            wordMap.put(element.getKey(), new WordNeuron(element.getKey(), element.getValue(),
                dimension));
        }
    
	}
	public void trainW(File sequenceFile) throws Exception, IOException{
    	double last_loss=0;
    	if(test_switch){
        	String temp212;
        	int rowNum=0;
        	File f=new File(W_DW);
    		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
    		while ((temp212 = br.readLine()) != null){
    			String[] strs = temp212.split(" ");
    			for(int i=0;i<strs.length;i++){
    				W[i][rowNum]=Double.parseDouble(strs[i]);
    			//	W[rowNum][i]=Double.parseDouble(strs[i]);
    			}
    			rowNum++;
    		}
    		test_switch=false;
    	}
    	else{
    	if(!init){	
    		preComputeBias();
    	}
    	if(init){
    			Random random = new Random(12345l);
    			for (int i = 0; i < dataNum; i++) {
    				for(int j=0;j<dimension;j++){
    					((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j] = random.nextDouble() / dimension;
    					((WordNeuron)wordMap.get(String.valueOf(i))).syn1neg[j] = random.nextDouble() / dimension;
    				}
    			}
    			for(int i=0;i<dataNum;i++){
    				double sumsyn0=0;
    				double sumsyn1neg=0;
    				for(int j=0;j<dimension;j++){
    					sumsyn0+=((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j]*((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j];
    					sumsyn1neg+=((WordNeuron)wordMap.get(String.valueOf(i))).syn1neg[j]*((WordNeuron)wordMap.get(String.valueOf(i))).syn1neg[j];
    				}
    				sumsyn0=Math.sqrt(sumsyn0);
    				sumsyn1neg=Math.sqrt(sumsyn1neg);
    				for(int j=0;j<dimension;j++){
    					((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j]/=sumsyn0;
    					((WordNeuron)wordMap.get(String.valueOf(i))).syn1neg[j]/=sumsyn1neg;
    				}
    			}
    			
    			
    			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sequenceFile))); {
                String temp = null;
                long nextRandom = 5;
                int wordCount = 0;
                int lastWordCount = 0;
                int wordCountActual = 0;
                while ((temp = br.readLine()) != null) {
                    if (wordCount - lastWordCount > 10000) {
                        System.out
                            .println("alpha:" + alpha + "\tProgress: "
                                     + (int) (wordCountActual / (double) (trainWordsCount + 1) * 100)
                                     + "%");
                        wordCountActual += wordCount - lastWordCount;
                        lastWordCount = wordCount;
                        alpha = startingAlpha * (1 - wordCountActual / (double) (trainWordsCount + 1));
                        if (alpha < startingAlpha * 0.0001) {
                            alpha = startingAlpha * 0.0001;
                        }
                    }
                    String[] strs = temp.split(" ");
                    wordCount += strs.length;
                    List<WordNeuron> sentence = new ArrayList<WordNeuron>();
                    for (int i = 0; i < strs.length; i++) {
                        Neuron entry = wordMap.get(strs[i]);
                        if (entry == null) {
                            continue;
                        }
                        // The subsampling randomly discards frequent words while keeping the ranking same
                        if (sample > 0) {
                            double ran = (Math.sqrt(entry.freq / (sample * trainWordsCount)) + 1)
                                         * (sample * trainWordsCount) / entry.freq;
                            nextRandom = nextRandom * 25214903917L + 11;
                            if (ran < (nextRandom & 0xFFFF) / (double) 65536) {
                                continue;
                            }
                        }
                        sentence.add((WordNeuron) entry);
                    }

                    for (int index = 0; index < sentence.size(); index++) {
                        nextRandom = nextRandom * 25214903917L + 11;
                        if (isCbow) {
                            cbowGram(index, sentence, (int) nextRandom % window);
                        } else {
                            skipGram(index, sentence, (int) nextRandom % window,nextRandom);
                        }
                    }

                }

                System.out.println("Vocab size: " + wordMap.size());
                System.out.println("Words in train file: " + trainWordsCount);
                System.out.println("sucess train over!");
            }
    	
    			for(int i=0;i<dataNum;i++){
    				WordNeuron wordn=(WordNeuron)wordMap.get(String.valueOf(i));
    				for(int j=0;j<dimension;j++){
    					W[i][j]=wordn.syn0[j];            //--------------W中的0-dataNum是否对应wordMap中的key值？
    				}
    			}
    			for(int i=0;i<dataNum;i++){
    				double sumW=0;
    				for(int j=0;j<dimension;j++){
    					sumW+=W[i][j]*W[i][j];
    				}
    				sumW=Math.sqrt(sumW);
    				for(int j=0;j<dimension;j++){
    					W[i][j]/=sumW;
    				}
    			}
        
    	}
    	else{
    			Random random = new Random(12345l);
    			for (int i = 0; i < dataNum; i++) {
    				for(int j=0;j<dimension;j++){
    					((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j] = random.nextDouble() / dimension;
    					((WordNeuron)wordMap.get(String.valueOf(i))).syn1neg[j] = random.nextDouble()  / dimension;
    				}
    			}
    			for(int i=0;i<dataNum;i++){
    				double sumsyn0=0;
    				double sumsyn1neg=0;
    				for(int j=0;j<dimension;j++){
    					sumsyn0+=((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j]*((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j];
    					sumsyn1neg+=((WordNeuron)wordMap.get(String.valueOf(i))).syn1neg[j]*((WordNeuron)wordMap.get(String.valueOf(i))).syn1neg[j];
    				}
    				sumsyn0=Math.sqrt(sumsyn0);
    				sumsyn1neg=Math.sqrt(sumsyn1neg);
    				for(int j=0;j<dimension;j++){
    					((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j]/=sumsyn0;
    					((WordNeuron)wordMap.get(String.valueOf(i))).syn1neg[j]/=sumsyn1neg;
    				}
    			}
    			
    			boolean syn01=true;
    			for(int i=0;i<dataNum;i++){
    				for(int j=0;j<dimension;j++){
    					W[i][j]+=alphaBias*(bias[i][j]);
    				}
    			}
    			for(int i=0;i<dataNum;i++){//把所有的syn0加上bias
    				for(int j=0;j<dimension;j++){
    					((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j]=W[i][j];
    				}
    			}
    			for(int loopNum=0;loopNum<LOOP;loopNum++){
            	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sequenceFile))); {
                String temp121 = null;
                long nextRandom = 5;
                int wordCount = 0;
                int lastWordCount = 0;
                int wordCountActual = 0;
                //alpha=startingAlpha;//----------------
                while ((temp121 = br.readLine()) != null) {
                    if (wordCount - lastWordCount > 10000) {
                        System.out
                            .println("alpha:" + alpha + "\tProgress: "
                                     + (int) (wordCountActual / (double) (trainWordsCount + 1) * 100)
                                     + "%");
                        wordCountActual += wordCount - lastWordCount;
                        lastWordCount = wordCount;
                        alpha = startingAlpha * (1 - wordCountActual / (double) (trainWordsCount + 1));
                        if (alpha < startingAlpha * 0.0001) {
                            alpha = startingAlpha * 0.0001;
                        }
                    }
                    String[] strs = temp121.split(" ");
                    wordCount += strs.length;
                    List<WordNeuron> sentence = new ArrayList<WordNeuron>();
                    for (int i = 0; i < strs.length; i++) {
                        Neuron entry = wordMap.get(strs[i]);
                        if (entry == null) {
                            continue;
                        }
                        // The subsampling randomly discards frequent words while keeping the ranking same
                        if (sample > 0) {
                            double ran = (Math.sqrt(entry.freq / (sample * trainWordsCount)) + 1)
                                         * (sample * trainWordsCount) / entry.freq;
                            nextRandom = nextRandom * 25214903917L + 11;
                            if (ran < (nextRandom & 0xFFFF) / (double) 65536) {
                                continue;
                            }
                        }
                        sentence.add((WordNeuron) entry);
                    }

                    for (int index = 0; index < sentence.size(); index++) {
                        nextRandom = nextRandom * 25214903917L + 11;
                        //skipGram(index, sentence, (int) nextRandom % window,nextRandom);
                        cbowSkip(index, sentence, (int) nextRandom % window,syn01,nextRandom);
                    }

                }
                System.out.println("Vocab size: " + wordMap.size());
                System.out.println("Words in train file: " + trainWordsCount);
                System.out.println("sucess train over!");
            }//至此syn1neg被影响好了
    			}
            	syn01=false;
            	Random random2 = new Random(12345l);
    			for (int i = 0; i < dataNum; i++) {
    				for(int j=0;j<dimension;j++){
    					((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j] = random2.nextDouble()/ dimension;
    					//((WordNeuron)wordMap.get(String.valueOf(i))).syn1neg[j]/=dimension;
    				}
    			}
    			for(int i=0;i<dataNum;i++){
    				double sumsyn0=0;
    				double sumsyn1neg=0;
    				for(int j=0;j<dimension;j++){
    					sumsyn0+=((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j]*((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j];
    					sumsyn1neg+=((WordNeuron)wordMap.get(String.valueOf(i))).syn1neg[j]*((WordNeuron)wordMap.get(String.valueOf(i))).syn1neg[j];
    				}
    				sumsyn0=Math.sqrt(sumsyn0);
    				sumsyn1neg=Math.sqrt(sumsyn1neg);
    				for(int j=0;j<dimension;j++){
    					((WordNeuron)wordMap.get(String.valueOf(i))).syn0[j]/=sumsyn0;
    					((WordNeuron)wordMap.get(String.valueOf(i))).syn1neg[j]/=sumsyn1neg;
    				}
    			}
    			
            	for(int loopNum=0;loopNum<LOOP;loopNum++){
            	BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(sequenceFile))); {
                    String temp121 = null;
                    long nextRandom = 5;
                    int wordCount = 0;
                    int lastWordCount = 0;
                    int wordCountActual = 0;
                    //alpha=startingAlpha;
                    while ((temp121 = br2.readLine()) != null) {
                        if (wordCount - lastWordCount > 10000) {
                            System.out
                                .println("alpha:" + alpha + "\tProgress: "
                                         + (int) (wordCountActual / (double) (trainWordsCount + 1) * 100)
                                         + "%");
                            wordCountActual += wordCount - lastWordCount;
                            lastWordCount = wordCount;
                            alpha = startingAlpha * (1 - wordCountActual / (double) (trainWordsCount + 1));
                            if (alpha < startingAlpha * 0.0001) {
                                alpha = startingAlpha * 0.0001;
                            }
                        }
                        String[] strs = temp121.split(" ");
                        wordCount += strs.length;
                        List<WordNeuron> sentence = new ArrayList<WordNeuron>();
                        for (int i = 0; i < strs.length; i++) {
                            Neuron entry = wordMap.get(strs[i]);
                            if (entry == null) {
                                continue;
                            }
                            // The subsampling randomly discards frequent words while keeping the ranking same
                            if (sample > 0) {
                                double ran = (Math.sqrt(entry.freq / (sample * trainWordsCount)) + 1)
                                             * (sample * trainWordsCount) / entry.freq;
                                nextRandom = nextRandom * 25214903917L + 11;
                                if (ran < (nextRandom & 0xFFFF) / (double) 65536) {
                                    continue;
                                }
                            }
                            sentence.add((WordNeuron) entry);//一个sentence就是一个sequence
                        }

                        for (int index = 0; index < sentence.size(); index++) { //对于一个sequence，所有点都作为一遍窗口中心点
                            nextRandom = nextRandom * 25214903917L + 11;
                            //skipGram(index, sentence, (int) nextRandom % window,nextRandom);
                            cbowSkip(index, sentence, (int) nextRandom % window,syn01,nextRandom);
                        }

                    }
                    System.out.println("Vocab size: " + wordMap.size());
                    System.out.println("Words in train file: " + trainWordsCount);
                    System.out.println("sucess train over!");
                }//至此使用syn1影响syn0结束
            	}
    			for(int i=0;i<dataNum;i++){
    				WordNeuron wordn=(WordNeuron)wordMap.get(String.valueOf(i));
    				for(int j=0;j<dimension;j++){
    					W[i][j]=wordn.syn0[j];   
    				}
    			}
    			for(int i=0;i<dataNum;i++){
    				double sumW=0;
    				for(int j=0;j<dimension;j++){
    					sumW+=W[i][j]*W[i][j];
    				}
    				sumW=Math.sqrt(sumW);
    				for(int j=0;j<dimension;j++){
    					W[i][j]/=sumW;
    				}
    			}
        
    	}
    	}
    	
    
	}
	public void InitUnigramTable(){
		double trainwordspow=0;
		double power=0.75;
		double d1=0;
		int countIndex=0;
		for(int i=0;i<dataNum;i++){
			trainwordspow+=Math.pow(wordMap.get(String.valueOf(i)).freq, power);
		}
		d1=Math.pow(wordMap.get(String.valueOf(countIndex)).freq, power)/trainwordspow;
		for(int j=0;j<table_size;j++){
			table[j]=countIndex;
			if(j/(double)table_size>d1){
				countIndex++;
				d1+=Math.pow(wordMap.get(String.valueOf(countIndex)).freq, power)/trainwordspow;
			}
			if(countIndex>=dataNum){
				countIndex=dataNum-1;
			}
		}
	}
	
    /**
     * skip gram 模型训练
     * @param sentence
     * @param neu1 
     */
    private void skipGram(int index, List<WordNeuron> sentence, int b, long nextRandom) {
        // TODO Auto-generated method stub
        WordNeuron word = sentence.get(index);
        int a, c = 0;

        for (a = b; a < window * 2 + 1 - b; a++) {
            if (a == window) {
                continue;
            }
            c = index - window + a;
            if (c < 0 || c >= sentence.size()) {
                continue;
            }
            
            double[] neu1e = new double[dimension];//误差项
            //HIERARCHICAL SOFTMAX
            List<Neuron> neurons = word.neurons;
            WordNeuron we = sentence.get(c);
            if(hs){
            	for (int i = 0; i < neurons.size(); i++) {
            		HiddenNeuron out = (HiddenNeuron) neurons.get(i);      //更新hiddenNeuron的syn1是否正确？
            		double f = 0;
            		// Propagate hidden -> output
            		for (int j = 0; j < dimension; j++) {
            			f += we.syn0[j] * out.syn1[j];
            		}
            		if (f <= -MAX_EXP || f >= MAX_EXP) {
            			continue;
            		} else {
            			f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);
            			f = expTable[(int) f];
            		}
            		// 'g' is the gradient multiplied by the learning rate
            		double g = (1 - word.codeArr[i] - f) * alpha;
            		// Propagate errors output -> hidden
            		for (c = 0; c < dimension; c++) {		
            			neu1e[c] += g * out.syn1[c];
            		}	
            		// Learn weights hidden -> output
            		for (c = 0; c < dimension; c++) {
                    out.syn1[c] += g * we.syn0[c];
            		}
            	}
            }
            //NEGATIVE SAMPLING
            if(NEG){
            	int label;
            	WordNeuron target;
            	for(int i=0;i<negSample+1;i++){
            		if(i==0){
            			target=word;
            			label=1;
            		}
            		else{
            			nextRandom = nextRandom * 25214903917L + 11;
            			int indexNum=table[Math.abs((int)((nextRandom>>16)%table_size))];
            			if(indexNum==0){
            				//System.err.println(indexNum);
            				indexNum=Math.abs((int)nextRandom%(dataNum-1)+1);
            			}
            			if(indexNum==Integer.valueOf(word.name)){
            				continue;
            			}
            			target=(WordNeuron)wordMap.get(String.valueOf(indexNum));
            			label=0;
            		}
            		double f=0;
            		for(int d=0;d<dimension;d++){
            			f+=we.syn0[d]*target.syn1neg[d];
            			//System.err.println(wordMap.size());
            		}
            		double g;
            		if(f>MAX_EXP)		{g=(label-1)*alpha;}
            		else if(f<-MAX_EXP)	{g=(label-0)*alpha;}
            		else				{g=(label - expTable[(int)((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))]) * alpha;};
                    for (int d = 0; d < dimension; d++) {
                    	neu1e[d] += g * target.syn1neg[d];
                    }
                    for (int d = 0; d < dimension; d++) {
                    	target.syn1neg[d] += g * we.syn0[d];
                    }
            	}
            }
            // Learn weights input -> hidden
            for (int j = 0; j < dimension; j++) {
                we.syn0[j] += neu1e[j];
            }
        }
    
    }
    /**
     * 词袋模型
     * @param index
     * @param sentence
     * @param b
     */
    private void cbowGram(int index, List<WordNeuron> sentence, int b) {
        WordNeuron word = sentence.get(index);
        int a, c = 0;

        List<Neuron> neurons = word.neurons;
        double[] neu1e = new double[dimension];//误差项
        double[] neu1 = new double[dimension];//误差项
        WordNeuron last_word;

        for (a = b; a < window * 2 + 1 - b; a++)
            if (a != window) {
                c = index - window + a;
                if (c < 0)
                    continue;
                if (c >= sentence.size())
                    continue;
                last_word = sentence.get(c);
                if (last_word == null)
                    continue;
                for (c = 0; c < dimension; c++)
                    neu1[c] += last_word.syn0[c];
            }

        //HIERARCHICAL SOFTMAX
        
        for (int d = 0; d < neurons.size(); d++) {
            HiddenNeuron out = (HiddenNeuron) neurons.get(d);
            double f = 0;
            // Propagate hidden -> output
            for (c = 0; c < dimension; c++)
                f += neu1[c] * out.syn1[c];
            if (f <= -MAX_EXP)
                continue;
            else if (f >= MAX_EXP)
                continue;
            else
                f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
            // 'g' is the gradient multiplied by the learning rate
            //            double g = (1 - word.codeArr[d] - f) * alpha;
            //              double g = f*(1-f)*( word.codeArr[i] - f) * alpha;
            double g = f * (1 - f) * (word.codeArr[d] - f) * alpha;
            //
            for (c = 0; c < dimension; c++) {
                neu1e[c] += g * out.syn1[c];
            }
            // Learn weights hidden -> output
            for (c = 0; c < dimension; c++) {
                out.syn1[c] += g * neu1[c];
            }
        }
        for (a = b; a < window * 2 + 1 - b; a++) {
            if (a != window) {
                c = index - window + a;
                if (c < 0)
                    continue;
                if (c >= sentence.size())
                    continue;
                last_word = sentence.get(c);
                if (last_word == null)
                    continue;
                for (c = 0; c < dimension; c++)
                    last_word.syn0[c] += neu1e[c];
            }

        }
    }
    /**
     * 在!init后进入的cbow+skip-gram更新函数
     * 
     * 
     * */
    public void cbowSkip(int index, List<WordNeuron> sentence, int b, boolean syn01, long nextRandom){
    	if(syn01){  //train syn1neg
            // TODO Auto-generated method stub
            WordNeuron word = sentence.get(index);
            int a, c = 0;
            for (a = b; a < window * 2 + 1 - b; a++) {
                if (a == window) {
                    continue;
                }
                c = index - window + a;
                if (c < 0 || c >= sentence.size()) {
                    continue;
                }
                
                double[] neu1e = new double[dimension];//误差项
                //HIERARCHICAL SOFTMAX
                List<Neuron> neurons = word.neurons;
                WordNeuron we = sentence.get(c);
                if(hs){
                for (int i = 0; i < neurons.size(); i++) {
                    HiddenNeuron out = (HiddenNeuron) neurons.get(i);
                    double f = 0;
                    // Propagate hidden -> output
                    for (int j = 0; j < dimension; j++) {
                        f += we.syn0[j] * out.syn1[j];
                    }
                    if (f <= -MAX_EXP || f >= MAX_EXP) {
                        continue;
                    } else {
                        f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);
                        f = expTable[(int) f];
                    }
                    // 'g' is the gradient multiplied by the learning rate
                    double g = (1 - word.codeArr[i] - f) * alpha;
                    // Propagate errors output -> hidden
                    for (c = 0; c < dimension; c++) {
                        neu1e[c] += g * out.syn1[c];
                    }
                    // Learn weights hidden -> output
                    for (c = 0; c < dimension; c++) {
                        out.syn1[c] += g * we.syn0[c];
                    }
                }
            }
                if(NEG){
                	int label;
                	WordNeuron target;
                	for(int i=0;i<negSample+1;i++){
                		if(i==0){
                			target=word;
                			label=1;
                		}
                		else{
                			nextRandom = nextRandom * 25214903917L + 11;
                			int indexNum=table[Math.abs((int)((nextRandom>>16)%table_size))];
                			if(indexNum==0){
                				//System.err.println(indexNum);
                				indexNum=Math.abs((int)nextRandom%(dataNum-1)+1);
                			}
                			if(indexNum==Integer.valueOf(word.name)){
                				continue;
                			}
                			target=(WordNeuron)wordMap.get(String.valueOf(indexNum));
                			label=0;
                		}
                		double f=0;
                		for(int d=0;d<dimension;d++){
                			f+=we.syn0[d]*target.syn1neg[d];
                			//System.err.println(wordMap.size());
                		}
                		double g;
                		if(f>MAX_EXP)		{g=(label-1)*alpha;}
                		else if(f<-MAX_EXP)	{g=(label-0)*alpha;}
                		else				{g=(label - expTable[(int)((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))]) * alpha;};
                        for (int d = 0; d < dimension; d++) {
                        	neu1e[d] += g * target.syn1neg[d];
                        }
                        for (int d = 0; d < dimension; d++) {
                        	target.syn1neg[d] += g * we.syn0[d];
                        }
                	}
                }
            }
    	}
    	else{  //train syn0
            // TODO Auto-generated method stub
            WordNeuron word = sentence.get(index);
            int a, c = 0;
            for (a = b; a < window * 2 + 1 - b; a++) {
                if (a == window) {
                    continue;
                }
                c = index - window + a;
                if (c < 0 || c >= sentence.size()) {
                    continue;
                }
                
                double[] neu1e = new double[dimension];//误差项
                //HIERARCHICAL SOFTMAX
                List<Neuron> neurons = word.neurons;
                WordNeuron we = sentence.get(c);
                if(hs){
                for (int i = 0; i < neurons.size(); i++) {
                    HiddenNeuron out = (HiddenNeuron) neurons.get(i);
                    double f = 0;
                    // Propagate hidden -> output
                    for (int j = 0; j < dimension; j++) {
                        f += we.syn0[j] * out.syn1[j];
                    }
                    if (f <= -MAX_EXP || f >= MAX_EXP) {
                        continue;
                    } else {
                        f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);
                        f = expTable[(int) f];
                    }
                    // 'g' is the gradient multiplied by the learning rate
                    double g = (1 - word.codeArr[i] - f) * alpha;
                    // Propagate errors output -> hidden
                    for (c = 0; c < dimension; c++) {
                        neu1e[c] += g * out.syn1[c];
                    }
                    
                }
                }
                if(NEG){
                	int label;
                	WordNeuron target;
                	for(int i=0;i<negSample+1;i++){
                		if(i==0){
                			target=word;
                			label=1;
                		}
                		else{
                			nextRandom = nextRandom * 25214903917L + 11;
                			int indexNum=table[Math.abs((int)((nextRandom>>16)%table_size))];
                			if(indexNum==0){
                				//System.err.println(indexNum);
                				indexNum=Math.abs((int)nextRandom%(dataNum-1)+1);
                			}
                			if(indexNum==Integer.valueOf(word.name)){
                				continue;
                			}
                			target=(WordNeuron)wordMap.get(String.valueOf(indexNum));
                			label=0;
                		}
                		double f=0;
                		for(int d=0;d<dimension;d++){
                			f+=we.syn0[d]*target.syn1neg[d];
                			//System.err.println(wordMap.size());
                		}
                		double g;
                		if(f>MAX_EXP)		{g=(label-1)*alpha;}
                		else if(f<-MAX_EXP)	{g=(label-0)*alpha;}
                		else				{g=(label - expTable[(int)((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))]) * alpha;};
                        for (int d = 0; d < dimension; d++) {
                        	neu1e[d] += g * target.syn1neg[d];
                        }
                	}
                
                }

                // Learn weights input -> hidden
                for (int j = 0; j < dimension; j++) {
                    we.syn0[j] += neu1e[j];
                }
            }
        
        }
    }
	
	public void makeTrainTest() throws Exception{
		int labels[]=new int[dataNum];
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
	public void writeVecor(File ftrain,File ftest,File all,File label) throws Exception{
		int labels[]=readLabels();
    	FileWriter fw=new FileWriter(ftrain);
    	FileWriter fwt=new FileWriter(ftest);
    	FileWriter flabel=new FileWriter(label);
    	for(int i=0;i<dataNum;i++){
    		if(TestTrain[i]==1){
    			flabel.write(String.valueOf(labels[i])+'\n');
    		for(int j=0;j<dimension;j++){
    			if(j!=dimension-1){
    				fw.write(String.valueOf(W[i][j])+" ");
    			}
    			else{
    				fw.write(String.valueOf(W[i][j])+'\n');
    			}
    		}
    	}
    		else{
        		for(int j=0;j<dimension;j++){
        			if(j!=dimension-1){
        				fwt.write(String.valueOf(W[i][j])+" ");
        			}
        			else{
        				fwt.write(String.valueOf(W[i][j])+'\n');
        			}
        		}
    		}
    	}
    	fw.close();
    	fwt.close();
    	flabel.close();
    	FileWriter fwall=new FileWriter(all);
    	for(int i=0;i<dataNum;i++){
    		for(int j=0;j<dimension;j++){
    			if(j!=dimension-1){
    				fwall.write(String.valueOf(W[i][j])+" ");
    			}
    			else{
    				fwall.write(String.valueOf(W[i][j])+'\n');
    			}
    		}
    	}
    	fwall.close();
	}
	public void preComputeBias() throws Exception{
		int labels[]=readLabels();
		System.out.println(saw.alphaB.length);
		int map[]=new int[saw.alphaB.length];
		int makeMap=0;
		for(int i=0;i<dataNum;i++){
			if(TestTrain[i]==1){
				map[makeMap]=i;
				makeMap++;
			}
		}
		double[][] alpha = saw.alphaB;
		FileWriter fw111=new FileWriter(new File(modelFile+"/Bias/"+source+"_alphaBiasLevel_"+alphaLevel+"_alpha_"+String.valueOf(flagNum)+"->"+String.valueOf(limitRandom*100)+"%"+".txt"));
		for(int i=0;i<alpha.length;i++){
			fw111.write(String.valueOf(saw.indexSvm[i])+" "+String.valueOf(labels[map[saw.indexSvm[i]]])+" ");
			for(int j=0;j<saw.nr_class;j++)
				fw111.write(String.valueOf(alpha[i][j])+" ");
			fw111.write('\n');
		}
		fw111.close();
    	double[][] weightSvm = saw.weightB;
    	for(int i = 0; i < saw.alphaB.length; i ++){
    		int id = map[saw.indexSvm[i]];
    		int label = labels[id];
    		//System.out.println(saw.nr_class);
    		for(int j = 0; j < saw.nr_class; j ++){
				double c = 0;
				if(j == label){
					c = Evaluate_SVM.C;
				}
    			for(int k = 0; k < dimension; k ++) 
    				bias[id][k] +=  (c-alpha[i][j])*(weightSvm[label][k]-weightSvm[j][k]);
    		}
    		double sum = 0;
    		for(int j = 0; j < dimension; j ++){
        		sum += bias[id][j]*bias[id][j];       	
        		}
    		sum = Math.sqrt(sum);
    		if(sum > 0){
	    		for(int j = 0; j < dimension; j ++){
	        		bias[id][j] /= sum;
	    		}
    		}
    	}
    	FileWriter twBias = new FileWriter(new File(modelFile+"/Bias/"+source+"_alphaBiasLevel_"+alphaLevel+"_Bias_"+String.valueOf(flagNum)+"->"+String.valueOf(limitRandom*100)+"%"+".txt"));
    	for(int i=0;i<dataNum;i++){
    		if(TestTrain[i]==1){
    			twBias.write(String.valueOf(i)+"\t"+String.valueOf(labels[i])+"\t");
    			for(int j = 0; j < dimension; j ++){
    				twBias.write(String.valueOf(bias[i][j])+"\t");
    			}
    			twBias.write("\n");
    		}
    	}
    	twBias.close();
	}
	public int[] readLabels() throws Exception{
		int labels[]=new int[dataNum];
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(labelFile)));
		String line = "";
		while((line = trUsers.readLine())!=null){
				String[] strs = line.split("\t");
				labels[Integer.valueOf(strs[0])]=Integer.valueOf(strs[1]);
		}
		trUsers.close();
		return labels;
	}
	
	public void run(){
		System.out.println("the "+flagNum+" alphaBias = "+alphaBias+'\t'+"lambda = "+lambda+'\t'+"alpha = "+alpha+'\t'+"limitRandom = "+limitRandom);
		int LoopSize = 10;
		for(int i=0;i<LoopSize;i++){
			countLoopTime=i;
			 long start = System.currentTimeMillis() ; 
		        try{
		        	if(i == 0){
		        		makeGraph(new File(sequenceFile)); 
		        		makeTrainTest();//TestTrain,0=test,1=train
		        	}else{
		        		trainW(new File(sequenceFile)); 
		        	}
		        	if(writeVec){
		        		writeVecor(new File(modelFile+"/vector/"+source+"_alphaBiasLevel_"+alphaLevel+"_vectortrain_"+String.valueOf(flagNum)+"->"+String.valueOf(limitRandom*100)+"%"+".txt"),
		        				   new File(modelFile+"/vector/"+source+"_alphaBiasLevel_"+alphaLevel+"_vectortest_" +String.valueOf(flagNum)+"->"+String.valueOf(limitRandom*100)+"%"+".txt"),
		        				   new File(modelFile+"/vector/"+source+"_alphaBiasLevel_"+alphaLevel+"_vectorall_"+String.valueOf(flagNum)+"->"+String.valueOf(limitRandom*100)+"%"+".txt"),
		        				   new File(modelFile+"/vector/"+source+"_alphaBiasLevel_"+alphaLevel+"_trainlabel_"+String.valueOf(flagNum)+"->"+String.valueOf(limitRandom*100)+"%"+".txt"));
		        		//writeVec=false;
		        	}
		        	System.out.println(i+"\tTrain embeddings over! use time "+(System.currentTimeMillis()-start));
		        	/*
		        	 * 之前训练Deepwalk
		        	 * 之后训练SVM
		        	 * 
		        	 * */
		        	Evaluate_SVM svm=new Evaluate_SVM(dataNum,dimension,W,labelFile,limitRandom,TestTrain);
		        	saw=svm.trainSvm(new File(modelFile+"/svm_model/"+source+"_alphaBiasLevel_"+alphaLevel+"_model_" +String.valueOf(flagNum)+"->"+String.valueOf(limitRandom*100)+"%"+".txt"));
		        	double resultReceive[]=svm.evaluateSvm();
		        	FileWriter frw = new FileWriter(new File(modelFile+"/result/"+source+"_alphaBiasLevel_"+alphaLevel+"_Result_"+String.valueOf(flagNum)+"->"+String.valueOf(limitRandom*100)+"%"+".txt"),true);
		        	frw.write("**********************************************************************"+'\n');
		        	frw.write("Iter "+i+'\n');
		        	frw.write("Right = "+resultReceive[0]+"   "+"TestData = "+resultReceive[1]+"   "+"Precision = "+resultReceive[2]*100+"%"+'\n');
		        	frw.write("**********************************************************************"+'\n'+'\n'+'\n');
				frw.close();
		        }
		        catch(Exception e){
		        	e.printStackTrace();
		        }
		}
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//********SWITCH**********
		test_switch=false;
		isCbow=false;
		NEG=true;
		hs=false;
		//********FIXED***********
		StoreAlphaWeight.dimensionForSVM=dimension;//fixed
		source=args[0];//1st input
		System.out.println("File type is : "+source);
		modelFile=args[1];//2nd input
		System.out.println("Folder of data is at : "+modelFile);
		alphaLevel=Integer.valueOf(args[2]);
		System.out.println("AlphaBias Level : "+alphaLevel);
		labelFile=modelFile+"/group/group_"+source+".txt";
		sequenceFile=modelFile+"/sequence/sequence_"+source+".txt";
		W_DW=modelFile+"/W_DW/W_"+source+"_deepwalk.txt";//*********CHANGE
		//********THREAD**********
		List<LearnWonline> lls=new ArrayList<LearnWonline>();
    	for(int i=0;i<9;i++){
    		LearnWonline ls=new LearnWonline();
    		ls.lambda=0.3;
    		ls.flagNum=i;
    		ls.alpha=0.025;
    		ls.startingAlpha=ls.alpha;
    		ls.alphaBias=25*Math.pow(10, alphaLevel);
    		ls.limitRandom=0.1+0.1*i;
    		lls.add(ls);
    	}
    	System.out.println("Number of data : "+dataNum);
    	for(int i=0;i<1;i++){
			LearnWonline lsls1=lls.get(i);
			lsls1.start();
    	}
	}

}

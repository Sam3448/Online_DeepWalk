package Learn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

public class FIndNeighbor {
	int dataNum;
	int dimension=200;
	double W_dw[][];
	double W_mmdw[][];
	int labels[];
	int sameAsLabel_dw[];
	int sameAsLabel_mmdw[];
	NeighborLabel nblmmdw[];
	NeighborLabel nbldw[];
	storeTitle st[];
	HashMap<String,String> rowToId=new HashMap<String,String>();
	HashMap<String,String> idToRow=new HashMap<String,String>();
	HashMap<String,String> missedTitle=new HashMap<String,String>();
	static int k_neighbor;
	File group;
	public FIndNeighbor  (File f_group, File deepwalk, File mmdw) throws Exception{
		this.group=f_group;
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(f_group)));
		String line = "";
		int countLine=0;
		while((line = trUsers.readLine())!=null){
				countLine++;
		}
		trUsers.close();
		this.dataNum=countLine;
		this.W_dw=new double[dataNum][dimension];
		this.W_mmdw=new double[dataNum][dimension];
		this.labels=new int[dataNum];
		this.sameAsLabel_dw=new int[dataNum];
		this.sameAsLabel_mmdw=new int[dataNum];
		this.nblmmdw=new NeighborLabel[dataNum];
		this.nbldw=new NeighborLabel[dataNum];
		this.st=new storeTitle[dataNum];
		this.readVec(deepwalk, mmdw);
		this.readLabels(f_group);
	}
	public void readVec(File deepwalk, File mmdw) throws Exception{
		String temp212;
    	int rowNum=0;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(deepwalk)));
		while ((temp212 = br.readLine()) != null){
			String[] strs = temp212.split(" ");
			for(int i=0;i<strs.length;i++){
				W_dw[i][rowNum]=Double.parseDouble(strs[i]);
			}
			rowNum++;
		}
		br.close();
		
		String temp;
		int countLine=0;
		BufferedReader br2 = new BufferedReader(
	            new InputStreamReader(new FileInputStream(mmdw)));
		while ((temp = br2.readLine()) != null){
			String[] strs = temp.split(" ");
				for(int i=0;i<dimension;i++)
					W_mmdw[countLine][i]=Double.parseDouble(strs[i]);
				countLine++;
		}
		br2.close();
	}
	public void readLabels(File f_group) throws Exception{
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(f_group)));
		String line = "";
		while((line = trUsers.readLine())!=null){
				String[] strs = line.split("\t");
				labels[Integer.valueOf(strs[0])]=Integer.valueOf(strs[1]);
		}
		trUsers.close();
	}
	
	public void deepwalkW(File dwneighborlabel) throws Exception{
		for(int i=0;i<dataNum;i++){
			double distance[]=new double[dataNum];
			double distanceCmp[]=new double[dataNum];
			for(int j=0;j<dataNum;j++){
				if(i!=j){
					double timeAdd1=0;
					double timeAdd2=0;
					double timeBoth=0;
					for(int k=0;k<dimension;k++){
						timeAdd1+=W_dw[i][k]*W_dw[i][k];
						timeAdd2+=W_dw[j][k]*W_dw[j][k];
						timeBoth+=W_dw[i][k]*W_dw[j][k];
					}
					distance[j]=timeBoth/(Math.sqrt(timeAdd1)*Math.sqrt(timeAdd2));
					distanceCmp[j]=timeBoth/(Math.sqrt(timeAdd1)*Math.sqrt(timeAdd2));
				}
				else{
					distance[j]=-1;
					distanceCmp[j]=-1;
				}
			}
			Arrays.sort(distance);//升序
			nbldw[i]=new NeighborLabel(k_neighbor);
			for(int c=0;c<k_neighbor;c++){
				int arrayNum=0;
				for(int j=0;j<dataNum;j++){
					if(distance[dataNum-1-c]==distanceCmp[j]){
						arrayNum=j;
						nbldw[i].neighborLabel[c]=labels[arrayNum];
						nbldw[i].neighborIndex[c]=arrayNum;
						break;
					}
				}
			}
		}
		FileWriter fw=new FileWriter(dwneighborlabel);
		for(int i=0;i<dataNum;i++){
			fw.write(String.valueOf(labels[i])+"     ");
			for(int j=0;j<k_neighbor;j++){
				fw.write(String.valueOf(nbldw[i].neighborLabel[j])+" ");
			}
			fw.write('\n');
		}
		fw.close();
		for(int i=0;i<dataNum;i++){
			for(int j=0;j<nbldw[i].neighborLabel.length;j++){
				if(nbldw[i].neighborLabel[j]==labels[i])
					sameAsLabel_dw[i]++;
			}
		}
	}
	public void MMDWW(File mmdwneighborlabel) throws Exception{
		for(int i=0;i<dataNum;i++){
			double distance[]=new double[dataNum];
			double distanceCmp[]=new double[dataNum];
			for(int j=0;j<dataNum;j++){
				if(i!=j){
					double timeAdd1=0;
					double timeAdd2=0;
					double timeBoth=0;
					for(int k=0;k<dimension;k++){
						timeAdd1+=W_mmdw[i][k]*W_mmdw[i][k];
						timeAdd2+=W_mmdw[j][k]*W_mmdw[j][k];
						timeBoth+=W_mmdw[i][k]*W_mmdw[j][k];
					}
					distance[j]=timeBoth/(Math.sqrt(timeAdd1)*Math.sqrt(timeAdd2));
					distanceCmp[j]=timeBoth/(Math.sqrt(timeAdd1)*Math.sqrt(timeAdd2));
				}
				else{
					distance[j]=-1;
					distanceCmp[j]=-1;
				}
			}
			Arrays.sort(distance);//升序
			nblmmdw[i]=new NeighborLabel(k_neighbor);
			for(int c=0;c<k_neighbor;c++){
				int arrayNum=0;
				for(int j=0;j<dataNum;j++){
					if(distance[dataNum-1-c]==distanceCmp[j]){
						arrayNum=j;
						nblmmdw[i].neighborLabel[c]=labels[arrayNum];
						nblmmdw[i].neighborIndex[c]=arrayNum;
						break;
					}
				}
			}
		}
		FileWriter fw=new FileWriter(mmdwneighborlabel);
		for(int i=0;i<dataNum;i++){
			fw.write(String.valueOf(labels[i])+"     ");
			for(int j=0;j<k_neighbor;j++){
				fw.write(String.valueOf(nblmmdw[i].neighborLabel[j])+" ");
			}
			fw.write('\n');
		}
		fw.close();
		for(int i=0;i<dataNum;i++){
			for(int j=0;j<nblmmdw[i].neighborLabel.length;j++){
				if(nblmmdw[i].neighborLabel[j]==labels[i])
					sameAsLabel_mmdw[i]++;
			}
		}
	}
	public void readTitleFile(File ftitle) throws Exception{
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(ftitle)));
		String line = "";
		int count=0;
		while((line = trUsers.readLine())!=null){
				String[] strs = line.split("\t");
				st[count]=new storeTitle();
				st[count].title=strs[0];
				st[count].label=strs[1];
				count++;
		}
		trUsers.close();
	}
	public void cmpWrite(File compare,File nullId) throws Exception{
		FileWriter fw=new FileWriter(compare);
		FileWriter fw2=new FileWriter(nullId);
		int count=0;
		for(int i=0;i<dataNum;i++){
			if(sameAsLabel_mmdw[i]-sameAsLabel_dw[i]>2){
				count++;
				fw.write(rowToId.get(String.valueOf(i))+"("+String.valueOf(sameAsLabel_mmdw[i])+" , "+String.valueOf(sameAsLabel_dw[i])+")"+'\t');
				fw.write(st[i].title+'\t'+st[i].label+'\n');
				fw.write("********************************MMDW********************************"+'\n');
				for(int j=0;j<nblmmdw[i].neighborIndex.length;j++){
					fw.write(rowToId.get(String.valueOf(nblmmdw[i].neighborIndex[j]))+'\t');
					if(st[nblmmdw[i].neighborIndex[j]].title.equals("  ")){
						fw2.write(rowToId.get(String.valueOf(nblmmdw[i].neighborIndex[j]))+'\n');
						if(missedTitle.containsKey(String.valueOf(rowToId.get(String.valueOf(nblmmdw[i].neighborIndex[j])))))
							st[nblmmdw[i].neighborIndex[j]].title=missedTitle.get(String.valueOf(rowToId.get(String.valueOf(nblmmdw[i].neighborIndex[j]))));
					}
					fw.write(st[nblmmdw[i].neighborIndex[j]].title+'\t'+st[nblmmdw[i].neighborIndex[j]].label+'\n');
				}
				fw.write("*********************************DW*********************************"+'\n');
				for(int j=0;j<nbldw[i].neighborIndex.length;j++){
					fw.write(rowToId.get(String.valueOf(nbldw[i].neighborIndex[j]))+'\t');
					if(st[nbldw[i].neighborIndex[j]].title.equals("  ")){
						fw2.write(rowToId.get(String.valueOf(nbldw[i].neighborIndex[j]))+'\n');
						if(missedTitle.containsKey(String.valueOf(rowToId.get(String.valueOf(nbldw[i].neighborIndex[j])))))
							st[nbldw[i].neighborIndex[j]].title=missedTitle.get(String.valueOf(rowToId.get(String.valueOf(nbldw[i].neighborIndex[j]))));
					}
					fw.write(st[nbldw[i].neighborIndex[j]].title+'\t'+st[nbldw[i].neighborIndex[j]].label+'\n');
				}
				fw.write('\n');
				fw.write('\n');
				fw.write('\n');
			}
		}
		System.err.println(count);
		fw.close();
		fw2.close();
	}
	public void makeMap(File map) throws Exception{
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(map)));
		String line = "";
		int count=0;
		while((line = trUsers.readLine())!=null){
				rowToId.put(String.valueOf(count),line);
				idToRow.put(line, String.valueOf(count));
				count++;
		}
		trUsers.close();
	
	}
	public void refreshTitle(File missedTitle) throws Exception{
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(missedTitle)));
		String line = "";
		int count=0;
		while((line = trUsers.readLine())!=null){
			String strs[]=line.split("\t");	
			this.missedTitle.put(strs[0],strs[1]);
		}
		trUsers.close();
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		k_neighbor=5;
		String source="cora";
		FIndNeighbor fnb=new FIndNeighbor(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/group/group_"+source+".txt"),
				new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/W_DW/W_"+source+"_deepwalk.txt"),
				new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/vector/"+source+"_MMDW_kneighbor.txt"));//先得产生cora_MMDW_kneighbor
		fnb.makeMap(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/title/map.txt"));
		fnb.deepwalkW(new File("/Users/zwc34/Documents/k_neighbor/"+source+"_deepwalk_kneighbor.txt"));
		fnb.MMDWW(new File("/Users/zwc34/Documents/k_neighbor/"+source+"_MMDW_kneighbor.txt"));
		fnb.readTitleFile(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/title/title_group.txt"));//cora
		fnb.refreshTitle(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/title/missedTitles.txt"));
		fnb.cmpWrite(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/title/"+source+"_cmp.txt"),
				new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/title/"+source+"_nullId.txt"));
	}

}
class NeighborLabel{
	int neighborLabel[];
	int neighborIndex[];
	public NeighborLabel(int k_neighbor){
		this.neighborLabel=new int[k_neighbor];
		this.neighborIndex=new int[k_neighbor];
	}
}
class storeTitle{
	String title;
	String label;
}

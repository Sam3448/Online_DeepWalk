package Estimate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class Line {
	int colNum;
	Link link[];
	public void countLine(File graph) throws Exception{
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(graph)));
		String line = "";
		int countLine=0;
		while((line = trUsers.readLine())!=null){
				countLine++;
		}
		trUsers.close();
		this.colNum=countLine;
		link=new Link[colNum];
	}
	public void readGraph(File graph) throws Exception, IOException{
		String temp;
		int count=0;
		BufferedReader br = new BufferedReader(
	            new InputStreamReader(new FileInputStream(graph)));
		while ((temp = br.readLine()) != null){
			String[] strs = temp.split("\t");
			link[count]=new Link();
			link[count].source=Integer.valueOf(strs[0]);
			link[count].target=Integer.valueOf(strs[1]);
			count++;
		}
		br.close();
		System.out.print(count+" = "+colNum);
	
	}
	public void sortGraph(File outFile) throws Exception{
		FileWriter fw=new FileWriter(outFile);
		int lineNum=0;
		for(int i=0;i<colNum;i++){
			//for(int j=0;j<colNum;j++){
				//if(link[j].source==lineNum){
					fw.write(String.valueOf(link[i].source)+" "+String.valueOf(link[i].target)+" "+String.valueOf(link[i].weight)+'\n');
					fw.write(String.valueOf(link[i].target)+" "+String.valueOf(link[i].source)+" "+String.valueOf(link[i].weight)+'\n');
				//}
			//}
			//lineNum++;
		}
		fw.close();
	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		File graph=new File("/Users/zwc34/Downloads/TADW-master-2/cora/graph.txt");
		File outFile=new File("/Users/zwc34/Downloads/TADW-master-2/cora/graph_sort.txt");
		Line li=new Line();
		li.countLine(graph);
		li.readGraph(graph);
		li.sortGraph(outFile);
	}

}
class Link{
	int source;
	int target;
	static int weight=1;
}

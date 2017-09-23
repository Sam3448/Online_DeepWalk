package Learn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class SortFb {

	int labels[]=new int[4039];
	public void readFbAttr() throws Exception{//fixed

		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/facebook/outattr.txt"))));
		String line = "";
		while((line = trUsers.readLine())!=null){
				String[] strs = line.split(" ");
				String strs3[]=strs[2].split("=");
				labels[Integer.valueOf(strs[0])]=Integer.valueOf(strs3[1]);
		}
		trUsers.close();
	
	}
	public void writeFbAttr() throws Exception{
		FileWriter fwww=new FileWriter(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/facebook/group_facebook_0.txt"));
		for(int i=0;i<labels.length;i++)
			if(i>=0&&i<=347)
				fwww.write(String.valueOf(i)+'\t'+String.valueOf(labels[i])+'\n');
		fwww.close();
	}
	public void readFbgraph() throws Exception{
		FileWriter fwbb=new FileWriter(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/facebook/graph_facebook_0.txt"));
		BufferedReader trUsers1 = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/facebook/0.edges"))));
		String line = "";
		while((line = trUsers1.readLine())!=null){
				String[] strs = line.split(" ");
				fwbb.write(strs[0]+'\t'+strs[1]+'\n');
		}
		trUsers1.close();
		fwbb.close();
	}
		
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		SortFb sf=new SortFb();
		sf.readFbAttr();
		sf.writeFbAttr();
		sf.readFbgraph();
	}

}

package Learn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class DealWiki {
	int label_ori[]=new int[20];
	int label_map[]=new int[20];
	int store[];
	public void readGroup() throws Exception{
		int count=0;
		for(int i=0;i<label_ori.length;i++)
			label_ori[i]=-1;
		BufferedReader trUsers = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/group/group.txt")));
		String line = "";
		while((line = trUsers.readLine())!=null){
				String[] strs = line.split("\t");
				label_ori[Integer.valueOf(strs[1])]=1;
		}
		trUsers.close();
		for(int i=0;i<label_ori.length;i++){
			label_map[i]=-1;
			if(label_ori[i]!=-1){
				label_map[i]=count;
				count++;
			}
		}
		for(int i=0;i<label_map.length;i++)
			System.out.println(i+"  "+label_map[i]);
		FileWriter fw1111=new FileWriter(new File("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/group/group_wiki_sort.txt"));
		BufferedReader trUsers2 = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/zwc34/Documents/Eclipse_Neon workspace/UserRepresentation_v2/data/group/group.txt")));
		String line2 = "";
		while((line2 = trUsers2.readLine())!=null){
				String[] strs = line2.split("\t");
				fw1111.write(strs[0]+"\t");
				fw1111.write(String.valueOf(label_map[Integer.valueOf(strs[1])])+'\n');
		}
		trUsers2.close();
		fw1111.close();
		
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DealWiki dw=new DealWiki();
		dw.readGroup();
	}

}

package Learn;

public class DealDrv extends Thread {
	double B[][];
	double graph[][];
	double result[][];
	int dataNum;
	int dimension;
	int flag;
	public DealDrv(double B[][], double graph[][],int dataNum, int dimension, int flag){
		this.B=B;
		this.graph=graph;
		this.dataNum=dataNum;
		this.dimension=dimension;
		result=new double[dimension][dataNum];
		this.flag=flag;
	}
	public void run(){
    	for(int i=flag*50;i<(flag+1)*50;i++){
    			for(int j=0;j<dataNum;j++)
    				for(int m=0;m<dataNum;m++){
    					if(graph[j][m]==0)
    						continue;
    					result[i][j]-=2*B[i][m]*graph[j][m];
    				}
    		
    	}
    	
	}
	public double[][] getResult(){
		return result;
	}
}

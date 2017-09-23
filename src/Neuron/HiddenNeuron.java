package Neuron;

public class HiddenNeuron extends Neuron{
    
    public double[] syn1 ; //hidden->out
    public double[] syn1bias;
    
    public HiddenNeuron(int layerSize){
        syn1 = new double[layerSize] ;
        syn1bias= new double[layerSize];
    }
    
}

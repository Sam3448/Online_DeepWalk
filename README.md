# UserRepresentation_Online_DeepWalk
Accomplished by Weicheng Zhang.

Online Max-margin DeepWalk

## Introduction

This method is a improvement for MMDW. In this project, I used neural network based DeepWalk instead of matrix factorization DeepWalk, and improve the efficiency a lot. 

## Datasets
  The dataset I used in this project are also Cora, Citeseer and Wiki. 
  * data/sequence/sequence_*.txt: a list of randomly generated sequences based on the the connection of the network.
  * data/group/group_*.txt: the category list of vertices.
  * data/vector/: the folder to save learnt vectors of vertices.
  * data/svm_model/:  the folder to save trained svm classifiers.
  * data/Bias/: the folder to save calculated biasVectors.
  * data/result/: the folder to classification results.
  * data/T-SNE/: the folder to accomplish data visualization.

## Parameters
   	The three parameters needed for input are: "dataset", "data_folder", "order_of_alphaBias".
   
    dataset is the name of the training data.
    data_folder is the path to the data.
    order_of_alphaBias is the weight of the parameter alphaBias. For more details, please refer this [paper](https://www.ijcai.org/Proceedings/16/Papers/547.pdf).
  
## More
For more related works on network representation learning, please refer to my [homepage](http://weichengzhang.co).

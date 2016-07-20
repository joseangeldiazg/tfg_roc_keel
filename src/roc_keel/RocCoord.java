/***********************************************************************

	This file is part of KEEL-software, the Data Mining tool for regression, 
	classification, clustering, pattern mining and so on.

	Copyright (C) 2004-2010
	
	F. Herrera (herrera@decsai.ugr.es)
    L. Sánchez (luciano@uniovi.es)
    J. Alcalá-Fdez (jalcala@decsai.ugr.es)
    S. García (sglopez@ujaen.es)
    A. Fernández (alberto.fernandez@ujaen.es)
    J. Luengo (julianlm@decsai.ugr.es)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see http://www.gnu.org/licenses/
  
**********************************************************************/
package roc_keel;

import java.util.ArrayList;
import java.util.List;

/**
 * <p> Implements methods to manage the ROC curves. 
 * @author joseangeldiazg (University of Granada) 
 * @version 1.0
 * </p>
 */
public class RocCoord 
{
   protected int tp;
   protected int fp;
   protected int tn;
   protected int fn;
   protected double auc;
   protected String coord;
   
   RocCoord()
   {
       tp=0;
       fp=0;
       tn=0;
       fn=0;
       coord = null;
   }
   
   /**
    * <p>
    * Get the coords of the roc curve.
    * </p>
    * @return String with coords of the ROC curve. 
    */
   public String getCoord()
   {
       return this.coord;
   }
   
   /**
    * <p>
    * Compute the number of elements for each class.
    * </p>
    * @param probabilities The matrix with the probabilities. 
    * @param realClasses Array of Strings with the real class for each pair of probabilities. 
    */
   
   public void buildTwoClassRoc(double[][] probabilities, String realClasses[])
   {
       this.sort(probabilities, realClasses, 1);
       
       char [] trueClass= new char [probabilities.length];
       
       int p=0;
       int n=0;
       
       for(int i=0; i<probabilities.length; i++)
       {
           
          if(realClasses[i].equals("positive"))
          {
             trueClass[i]='P'; 
             p++;
          }     
          else if(realClasses[i].equals("negative"))
          {
              trueClass[i]='N';
              n++;
          }
          
       }
       
       computeCoordsRoc(trueClass,p,n);
   }
   
   /**
    * <p>
    * Creates a two-class model based on one of several classes.
    * </p>
    * @param probabilities The matrix with the probabilities. 
    * @param realClasses Array of Strings with the real class for each pair of probabilities.
    * @param differentClasses  Array with the different values of the classes in the real problem.
    * @param classA Index of the class that must be confronted with others. 
    */
   
   
   public void buildClassVsAllClassRoc(double[][] probabilities, String[] realClasses, 
           String[] differentClasses, int classA)
   {
       double[][] resultProbabilities = new double [probabilities.length][2];
       String []  resultRealClasses = new String [probabilities.length];

       double max=0;
       double [] norm = new double[2];
       
       
       
       for(int i=0; i<probabilities.length;i++)
       {
            resultProbabilities[i][0]=probabilities[i][classA];
             
            if(realClasses[i].equals(differentClasses[classA]))
            {
                resultRealClasses[i]="positive";
            }
            else
            {
               resultRealClasses[i]="negative";
            }
            
            for(int j=0; j<probabilities[i].length;j++)
            {
                if(j!=classA)
                {
                   if(probabilities[i][j]>max)
                   {
                       max=probabilities[i][j];
                   } 
                }            
            }
            norm[0]=probabilities[i][classA];
            norm[1]=max;           
       
            norm=normalize(norm);
            
            resultProbabilities[i][0]=norm[0];
            resultProbabilities[i][1]=norm[1];
            max=0;
       }
       
       this.buildTwoClassRoc(resultProbabilities, resultRealClasses);
   }
   
  
   /**
    * <p>
    * Obtain the coordinates of the ROC and the value of the AUC.
    * </p>
    * @param trueClass Array with the value of the real class for each row. 
    * @param p Number of positive examples.
    * @param n  Number of negative examples.
    */
   
   public void computeCoordsRoc(char [] trueClass, int p, int n )
   {
       
       double moveX=1.0/n;
       double moveY=1.0/p;
       double x=0;
       double y=0;
       double auc=0;
       double width=0;
       double widthAcumulate=0;
       
       
       this.coord="coordinates { (0,0)";
       
       
       for(int i=0; i<trueClass.length;i++)
       {
          
           if(trueClass[i]=='N')
           {
               x+=moveX;
               
               width=(x-widthAcumulate);
               widthAcumulate+=width;
               auc=auc+(double)((width*y));
           }
           //false negative
           else if(trueClass[i]=='P')
           {      
               y+=moveY;  
           }
            this.coord+="("+x+","+y+")";
        }   
       
        this.coord+=" };";
      
        this.auc=auc;
        isPerfect(this.coord);
      
    }
   
   /**
    * <p>
    * Obtain the coordinates when the problem is perfect. 
    * </p>
    * @param coord Coords to check. 
    */
   
   public void isPerfect(String coord)
   {
       if(coord.contains("(0.0,1.0)")||coord.contains("(0.0,1.00"))
       {
           this.coord="coordinates { (0,0)(0,1)(1,1)};";
           this.auc=1;
       }
   }
   
  /**
   *<p>
   * Normalizes the doubles in the array using the given value.
   *</p>
   * 
   * @param doubles the array of double
   * @return the doubles normalize to the rank 0-1 with sum(rank)>1
   */
   
   public double[] normalize(double[] doubles)
   {
      
        double normalize[];
        normalize = new double[doubles.length];


        double max= max(doubles);
        double min= min(doubles);


        for(int i=0; i<doubles.length; i++)
        {
            normalize[i]=((doubles[i])-(min))/((max)-(min));  
        }
  
    return normalize2(normalize);
  }
  
   /**
   *<p>
   * Normalizes the doubles in the array using the given value.
   *</p>
   * 
   * @param doubles the array of double
   * @return the doubles normalize to the rank 0-1 with sum(rank)=1
   */
  
  public double[] normalize2(double[] doubles) {
      
    double normalize[];
    normalize = new double[doubles.length];
    
    double total=0;
    
    for(int i=0; i<doubles.length; i++)
    {
        total+=doubles[i];  
    }
    
    for(int i=0; i<doubles.length; i++)
    {
        normalize[i]=doubles[i]/total;  
    }
    
    
    return normalize;
  }
  
   /**
   *<p>
   * Find the min value in a doubles array.
   *</p>
   *
   * @param doubles the array of double
   * @return min value of the array
   */
  
  public double min(double[] doubles) 
    { 
        double resultado = 0; 
        for(int i=0; i<doubles.length; i++) 
        { 
            if(doubles[i] < resultado) 
            { 
                resultado = doubles[i]; 
            } 
        } 
        
        return resultado; 
    } 

    
  /**
   *<p>
   * Find the max value in a doubles array.
   *</p>
   *
   * @param doubles the array of double
   * @return max value of the array
   */
    public double max(double[] doubles) 
    { 
        double resultado =0; 
        for(int i=0; i<doubles.length; i++) 
        { 
            if(doubles[i] > resultado) 
            { 
                resultado = doubles[i]; 
            } 
        } 
        
        return resultado; 
    }
    
  /**
   *<p>
   * Sort the probabilities and the realclasses acording to the max value of the probabilities.
   *</p>
   * @param realClasses The array with the strings (real classes) to sort. 
   * @param probabilities The matrix with the probabilities to sort. 
   * @param col Column on which sort 
   */
    
    
    public void sort(double[][] probabilities, String[] realClasses, int col) 
    {
    
        if (col < 0 || col > probabilities[0].length)
        {
            return;
        }

        double auxP;
        String auxC;

        for (int i = 0; i < probabilities.length; i++) 
        {
            for (int j = i + 1; j < probabilities.length; j++) 
            {
               
                
                if (probabilities[i][col]>probabilities[j][col]) 
                {
                    
                    auxC = realClasses[i];
                    realClasses[i] = realClasses[j];
                    realClasses[j] = auxC;

                    for (int k = 0; k < probabilities[0].length; k++) 
                    {
                        auxP = probabilities[i][k];
                        probabilities[i][k] = probabilities[j][k];
                        probabilities[j][k] = auxP;
                        
                    }
                }
            }
        }
    }
}

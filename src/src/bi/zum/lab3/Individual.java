package bi.zum.lab3;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.data.StateSpace;
import cz.cvut.fit.zum.util.Pair;
import cz.cvut.fit.zum.data.Edge;
import cz.cvut.fit.zum.data.Nodes;
import cz.cvut.fit.zum.api.Node;

import java.util.Random;
import java.util.Collections;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Comparator;

/**
 * @author David Omrai
 */

class SortByArrList implements Comparator<ArrayList<Integer>>{
    public int compare(ArrayList<Integer> a, ArrayList<Integer> b){
        if (b.get(1) == a.get(1)){
            return a.get(0) - b.get(0);
        }
        return b.get(1) - a.get(1);
    }
}

public class Individual extends AbstractIndividual {

    private double fitness = Double.NaN;
    private AbstractEvolution evolution;
    //genotype, array of boolean
    private boolean [] gene;
    

    /**
     * Creates a new individual
     * 
     * @param evolution The evolution object
     * @param randomInit <code>true</code> if the individual should be
     * initialized randomly (we do wish to initialize if we copy the individual)
     */
    public Individual(AbstractEvolution evolution, boolean randomInit) {
        this.evolution = evolution;
        
        if(randomInit) {
            Random r = new Random();
            int N = StateSpace.nodesCount();
            this.gene = new boolean[N];
            //generate random boolean vector
            for (int i = 0; i<N; i+=1){
                this.gene[i] = r.nextBoolean();
            }
            //repair if genotype is not correct, missing some roads
            scanAndRepairOperator();
        }
    }
    
    @Override
    public boolean isNodeSelected(int j) {       
        if ( (this.gene.length > j) && (this.gene[j] == true) )
            return true;
        return false;
    }
    
    /* old implementation
    / **
    * Break the genotype to make it possibly invalid, i.e. try to reduce nodes
    * just for testing, it's not used in program
    * /
    private void un_repair() {
        Random r = new Random();
        / * We iterate over all the edges * /
        for(Edge e : StateSpace.getEdges()) {
            if (gene[e.getFromId()] == true && gene[e.getToId()] == true){
                if (r.nextBoolean() == true)
                    gene[e.getFromId()] = false;
                else
                    gene[e.getToId()]   = false;
            }
        }
    }
    */
    
    /* old implementaion
    / **
    * Repairs the genotype to make it valid, i.e. ensures all the edges
    * are in the vertex cover.
    * /
    private void repair() {
        Random r = new Random();
        / * We iterate over all the edges * /
        
        for(Edge e : StateSpace.getEdges()) {
            if (gene[e.getFromId()] == false && gene[e.getToId()] == false){
                if (r.nextBoolean() == true)
                    gene[e.getFromId()] = true;
                else
                    gene[e.getToId()]   = true;
            }
        }
    }
    */
    
    /**
     * Method returns number of uncovered edges of given node
     * @param n node
     * @return number of uncovered edges
     */
    private int getUncoveredEdgesNum(Node n){
        int neighNum = 0;
        if (gene[n.getId()] == false){
            for (Edge e: n.getEdges()){
                
                if (gene[e.getFromId()] == false && gene[e.getToId()] == false){
                    neighNum+=1;
                }
            }
        }
        return neighNum;
    }
    
    /**
     * Method returns number of present neighbors of given node
     * @param n node
     * @return number of present neighbors
     */
    private int getNeighborsNum(Node n){
        int neighNum = 0;
        
        for (Edge e: n.getEdges()){
            if (gene[e.getToId()] == true && gene[e.getFromId()] == true){
                neighNum+=1;
            }
        }
        return neighNum;
    }
    
    /**
     * Scans genotype, stores all possibly needed vertexes
     * then iterate through them and pick just those with
     * highest number of neighbors
     */
    private void scanAndRepairOperator(){
        int num = 0;
        ArrayList<Integer> tmp;
        TreeSet<ArrayList<Integer>> nodeTree = new TreeSet<ArrayList<Integer>>(new SortByArrList());
        ArrayList<ArrayList<Integer>> nodeArr;
        nodeArr = new ArrayList<ArrayList<Integer>>();
         
        //Append array with nodes with uncovered edges
        for (Node n: StateSpace.getNodes()){
            if (gene[n.getId()] == false){
                
                num = getUncoveredEdgesNum(n);
                if (num != 0){
                    tmp = new ArrayList<Integer>();
                    tmp.add(n.getId());
                    tmp.add(num);
                    nodeTree.add(tmp);
                    nodeArr.add(tmp);
                }
           }
        }
        
        //repair broken genotype
        int nodeId = 0;
        int uncEdgs = 0;
        if (nodeTree.size() != 0){
            while(nodeTree.size()!=0){
                tmp = nodeTree.pollFirst();
                nodeId = tmp.get(0);
                uncEdgs = tmp.get(1);
                num = getUncoveredEdgesNum(StateSpace.getNode(nodeId));
                //System.out.print("uncEdgs: "+uncEdgs+" num:"+num+" nodeid:"+nodeId+"\n");
                
                //genotype is repaired
                if (num==0){
                    continue;
                }
                
                //insert new size of this node to tree
                if (uncEdgs!=num){
                    tmp = new ArrayList<Integer>();
                    tmp.add(nodeId);
                    tmp.add(num);
                    nodeTree.add(tmp);
                }
                else{
                    gene[nodeId] = true;
                }
            }
        }      
    }
    
    /**
     * Stores all redundant vertexes to sorted array
     * then remove those with smallest amount of neighbors
     */
    public void localOptimumOperator(){
        int num = 0;
        ArrayList<Integer> tmp;
        TreeSet<ArrayList<Integer>> nodeTree = new TreeSet<ArrayList<Integer>>(new SortByArrList());
        
        //Append array with nodes with all their neighbors present
        for (Node n: StateSpace.getNodes()){
            num = getNeighborsNum(n);
            if (num == n.getEdges().size()){
                tmp = new ArrayList<Integer>();
                tmp.add(n.getId());
                tmp.add(num);
                nodeTree.add(tmp);
            }
        }
        
        //remove redundant nodes, from those with least to ones with the most neighbors
        int nodeId = 0;
        int uncEdgs = 0;
        
        if (nodeTree.size() != 0){
            while(nodeTree.size()!=0){
                tmp = nodeTree.pollLast();
                nodeId = tmp.get(0);
                uncEdgs = tmp.get(1);
                num = getNeighborsNum(StateSpace.getNode(nodeId));
                
                //node is not redundant
                if (num!=uncEdgs){
                    continue;
                }
                               
                //remove redundant node
                gene[nodeId] = false;
            }
        }
    }
    
    /**
     * Evaluate the value of the fitness function for the individual. After
     * the fitness is computed, the <code>getFitness</code> may be called
     * repeatedly, saving computation time.
     */
    @Override
    public void computeFitness() {
        int N = StateSpace.nodesCount();
        //count present nodes in genotype
        int nodes = 0;
        for (int i = 0; i < N; i+=1){
            if (gene[i] == false)
                nodes+=1;
        }
        //save to fitness parameter
        this.fitness = (nodes); //all edges/present nodes StateSpace.edgesCount()
    }

    /**
     * Only return the computed fitness value
     *
     * @return value of fitness function
     */
    @Override
    public double getFitness() {
        return this.fitness;
    }

    /**
     * Does random changes in the individual's genotype, taking mutation
     * probability into account.
     * 
     * @param mutationRate Probability of a bit being inverted, i.e. a node
     * being added to/removed from the vertex cover.
     */
    @Override
    public void mutate(double mutationRate) {
        Random r = new Random();
        int N = StateSpace.nodesCount();
        //iterate through gene and change random nodes
        for (int i = 0; i < N; i+=1){
            if (r.nextDouble() < mutationRate)
                this.gene[i] = !this.gene[i];
        }
        //repair if mutation broke genotype
        scanAndRepairOperator();
    }
    
    /**
     * Crosses the current individual over with other individual given as a
     * parameter, yielding a pair of off-springs.
     * 
     * @param other The other individual to be crossed over with
     * @return A couple of offspring individuals
     */
    @Override
    public Pair crossover(AbstractIndividual other) {
        
        //Individual otherI = (Individual)other;
        Pair<Individual,Individual> result = new Pair();
        Random r = new Random();
        int N = StateSpace.nodesCount();
        boolean [] aGene = new boolean[N];
        boolean [] bGene = new boolean[N];
        
        
        /*
        //random bit crossober
        for (int i = 0; i < this.gene.length; i+=1){
            if (r.nextBoolean() == true){
                aGene[i] = this.isNodeSelected(i);
                bGene[i] = other.isNodeSelected(i);
            }
            else{
                aGene[i] = other.isNodeSelected(i);
                bGene[i] = this.isNodeSelected(i);
            }
        }
        */
        /*
        //two point crossover
        int fC;
        int sC;
        do{
            int tmp;
            fC = r.nextInt(N);
            sC = r.nextInt(N);
            tmp = fC;
            if (fC>sC){
                fC = sC;
                sC = tmp;
            }
        }while(fC!=sC);
        //exchange genes
        for (int i = 0; i < fC; i+=1){
            aGene[i] = this.isNodeSelected(i);
            bGene[i] = other.isNodeSelected(i);
        }
        for (int i = fC; i < sC; i+=1){
            aGene[i] = other.isNodeSelected(i);
            bGene[i] = this.isNodeSelected(i);
        }
        for (int i = sC; i < N; i+=1 ){
            aGene[i] = this.isNodeSelected(i);
            bGene[i] = other.isNodeSelected(i);
        }
        */
        
        //one point crossover
        int pointCross = r.nextInt( N/2 )  + (N/4);
        
        for (int i = 0; i < pointCross; i+=1){
            aGene[i] = this.isNodeSelected(i);
            bGene[i] = other.isNodeSelected(i);
        }
        for (int i = pointCross; i < N; i+=1){
            aGene[i] = other.isNodeSelected(i);
            bGene[i] = this.isNodeSelected(i);
        }
        
        
        result.a = new Individual(evolution, false);
        result.b = new Individual(evolution, false);
        result.a.gene = new boolean[this.gene.length];
        result.b.gene = new boolean[this.gene.length];
        for (int i = 0; i < this.gene.length; i+=1){
            result.a.gene[i] = aGene[i];
            result.b.gene[i] = bGene[i];
        }
        //repair if genotypes are broken
        result.a.scanAndRepairOperator();
        result.b.scanAndRepairOperator();
        return result;
    }

    
    /**
     * When you are changing an individual (eg. at crossover) you probably don't
     * want to affect the old one (you don't want to destruct it). So you have
     * to implement "deep copy" of this object.
     *
     * @return identical individual
     */
    @Override
    public Individual deepCopy() {
        Individual newOne = new Individual(evolution, false);
        //copy genotype
        newOne.gene = new boolean[this.gene.length];
        for (int i = 0; i < this.gene.length; i+=1){
            newOne.gene[i] = this.gene[i];
        }

        // for primitive types int, double, ...
        // newOne.val = this.val;

        // for objects (String, ...)
        // for your own objects you have to implement clone (override original inherited from Objcet)
        // newOne.infoObj = thi.infoObj.clone();

        // for arrays and collections (ArrayList, int[], Node[]...)
        /*
         // new array of the same length
         newOne.pole = new MyObjects[this.pole.length];		
         // clone all items
         for (int i = 0; i < this.pole.length; i++) {
         newOne.pole[i] = this.pole[i].clone(); // object
         // in case of array of primitive types - direct assign
         //newOne.pole[i] = this.pole[i]; 
         }
         // for collections -> make new instance and clone in for/foreach cycle all members from old to new
         */
        //copy fitness
        newOne.fitness = this.fitness;
        return newOne;
    }

    /**
     * Return a string representation of the individual.
     *
     * @return The string representing this object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        
        /* TODO: implement own string representation, such as a comma-separated
         * list of indices of nodes in the vertex cover
         */
        sb.append("gene:{|");
        for (int i = 0; i < this.gene.length; i+=1)
            sb.append(""+i+":"+this.gene[i]+"|");
        sb.append("}\n");
        
        sb.append(super.toString());

        return sb.toString();
    }
}

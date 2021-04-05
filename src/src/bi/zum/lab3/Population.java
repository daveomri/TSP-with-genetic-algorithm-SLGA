package bi.zum.lab3;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.api.ga.AbstractPopulation;
//import cz.cvut.fit.zum.data.StateSpace;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.LinkedHashSet;
//import java.util.Collections;
import java.util.Set;
//import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * @author David Omrai
 */
public class Population extends AbstractPopulation {

    public Population(AbstractEvolution evolution, int size) {
        individuals = new Individual[size];
        for (int i = 0; i < individuals.length; i++) {
            individuals[i] = new Individual(evolution, true);
            individuals[i].computeFitness();
        }
    }

    /**
     * Method to select individuals from population
     *
     * @param count The number of individuals to be selected
     * @return List of selected individuals
     */
    public List<AbstractIndividual> selectIndividuals(int count) {
        ArrayList<AbstractIndividual> selected = new ArrayList<AbstractIndividual>();
        //Arrays.sort(individuals);
        /* Old random version
        // example of random selection of N individuals
        AbstractIndividual individual = individuals[r.nextInt(individuals.length)];
        while (selected.size() < count) {
            selected.add(individual);
            individual = individuals[r.nextInt(individuals.length)];
        }
        */
        
        //rulete selection
        Set<AbstractIndividual> noDupl = new LinkedHashSet<>();
        Random r = new Random();
        double fitnessSum = 0.0;
        for (int i = 0; i < individuals.length; i+=1){
            fitnessSum+=individuals[i].getFitness();
        }
        /**
         * Iterate through individuals count-times
         * probability of selection is based on
         * fitness value, higher means better probability
         */
        while (noDupl.size() < count){
            double prevProb = 0.0;
            double randNum = r.nextDouble();
            for (int i = 0; i < individuals.length; i+=1){
                prevProb += (individuals[i].getFitness()/fitnessSum);
                if (prevProb < randNum){
                    noDupl.add(individuals[i]);
                }
            }
        }
        //selected now has count unique individuals
        selected.addAll(noDupl);
        
        return selected;
    }
}

package org.heigit.ors.routing.graphhopper.extensions.util;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import org.heigit.ors.routing.graphhopper.extensions.storages.AveragedMultiTreeSPEntry;

import java.util.PriorityQueue;

public class MultiSourceStoppingCriterion {
    private int treeEntrySize;
    private AveragedMultiTreeSPEntry combinedUnsettled;
    private IntHashSet targetSet;
    IntObjectMap<AveragedMultiTreeSPEntry> targetMap;
    IntObjectMap<Boolean> allTargetsForSourceFound;


    public MultiSourceStoppingCriterion(IntHashSet targetSet, IntObjectMap<AveragedMultiTreeSPEntry> targetMap, int treeEntrySize) {
        this.targetSet = targetSet;
        this.targetMap = targetMap;
        this.treeEntrySize = treeEntrySize;
        this.allTargetsForSourceFound = new IntObjectHashMap<>(treeEntrySize);
    }
    public boolean isFinished(AveragedMultiTreeSPEntry currEdge, PriorityQueue<AveragedMultiTreeSPEntry> prioQueue) {
        if(combinedUnsettled != null && checkAllTargetsForAllSourcesFound())
            return !queueHasSmallerWeight(combinedUnsettled, prioQueue);

        if (!targetSet.contains(currEdge.getAdjNode()))
            return false;

        setSourceTargetsFound();
        createCombinedUnsettled();

        return false;
    }

    /**
     * Create a single non-real target entry that combines all individual max weights of the unsettled nodes
     * This is done to minimize the iterations over the unsettled targets, which need to be done for correctness
     * until the prioQueue has no more possible better values
     */
    private void createCombinedUnsettled() {
        if(this.combinedUnsettled == null)
            this.combinedUnsettled = initCombinedUnsettled();
        updateCombinedUnsettled();
    }

    private AveragedMultiTreeSPEntry initCombinedUnsettled(){
        AveragedMultiTreeSPEntry combinedUnsettledTarget = new AveragedMultiTreeSPEntry(-1, -1, -1.0, false, null, treeEntrySize);
        //Set all weights to low start weight
        for (int i = 0; i < treeEntrySize; ++i)
            combinedUnsettledTarget.getItem(i).setWeight(-1.0);

        return combinedUnsettledTarget;
    }

    public void updateCombinedUnsettled(){
        if(combinedUnsettled == null)
            return;
        for(IntObjectCursor<AveragedMultiTreeSPEntry> entry : targetMap){
            for (int source = 0; source < treeEntrySize; ++source) {
                if(allTargetsForSourceFound.getOrDefault(source, false)) {

                    double entryWeight = entry.value.getItem(source).getWeight();

                    if (entryWeight > this.combinedUnsettled.getItem(source).getWeight()) {
                        this.combinedUnsettled.getItem(source).setWeight(entryWeight);
                    }
                }
            }
        }
    }

    /**
     * Check whether the priorityqueue has an entry that could possibly lead to a shorter path for any of the subItems
     * @return
     */
    private boolean queueHasSmallerWeight(AveragedMultiTreeSPEntry target, PriorityQueue<AveragedMultiTreeSPEntry> prioQueue) {
        for (AveragedMultiTreeSPEntry entry : prioQueue) {
            for (int i = 0; i < treeEntrySize; ++i) {
                if(entry.getItem(i).getWeight() < target.getItem(i).getWeight())
                    return true;
            }
        }
        return false;
    }

    private boolean checkAllTargetsForAllSourcesFound(){
        for (int source = 0; source < treeEntrySize; source++){
            if(combinedUnsettled.getItem(source).getWeight() == -1.0)
                return false;
        }
        return true;
    }

    private void setSourceTargetsFound() {
        for(int source = 0; source < treeEntrySize; source += 1){
            if(allTargetsForSourceFound.getOrDefault(source, false))
                continue;
            boolean allFound = true;
            for (IntCursor targetId : targetSet) {
                //The target has not been reached yet
                if(!targetMap.containsKey(targetId.value))
                    return;
                AveragedMultiTreeSPEntry target = targetMap.get(targetId.value);
                if(target.getItem(source).getWeight() == Double.POSITIVE_INFINITY) {
                    allFound = false;
                    break;
                }

            }
            allTargetsForSourceFound.put(source, allFound);
        }
    }

    public boolean isEntryLargerThanAllTargets(int source, double weight) {
        return combinedUnsettled != null
                && combinedUnsettled.getItem(source).getWeight() != -1.0
                && weight > combinedUnsettled.getItem(source).getWeight();
    }
}

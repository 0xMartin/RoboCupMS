package com.robogames.RoboCupMS.Module.OrderManagement.Bussiness.Object;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Entity.Playground;
import com.robogames.RoboCupMS.Entity.RobotMatch;
import com.robogames.RoboCupMS.Repository.RobotMatchRepository;

/**
 * Fronta zapasu, ktere cekaji na odehrani
 */
public class MatchQueue {

    /**
     * Hriste, na kterem se bude hrat zapas
     */
    private final Playground playground;

    /**
     * List cekajicich zapasu
     */
    private final LinkedList<RobotMatch> queue;

    /**
     * Fronta zapasu, ktere cekaji na odehrani
     * 
     * @param _discipline Disciplina, ve ktere se zapasi konaji
     */
    public MatchQueue(Playground _playground) {
        this.playground = _playground;
        this.queue = new LinkedList<RobotMatch>();
    }

    /**
     * Navrati disciplinu, ve ktere se hraje
     * 
     * @return Disciplina
     */
    public Playground getPlayground() {
        return this.playground;
    }

    /**
     * Navrati seznam cekajicich
     * 
     * @return Seznam zapasu cekajicich na odehrani
     */
    public List<RobotMatch> getMatches() {
        return this.queue;
    }

    /**
     * Synchronizuje stavy zapasu ve fronte ze stavy zapasu v databazi
     * 
     * @param repository RobotMatchRepository
     */
    public synchronized void synchronize(RobotMatchRepository repository) {
        // ID zapasu, ktere se v DB uz nenachazeji, ale ve fronte ano a musi byt tak
        // odstraneny
        List<Integer> indexes = new ArrayList<Integer>();

        // synchronizace stavu
        int index = 0;
        for(RobotMatch m : this.queue) {
            Optional<RobotMatch> match = repository.findById(m.getID());
            if (match.isPresent()) {
                RobotMatch dbMatch = match.get();
                m.setMatchState(dbMatch.getState());
                // Also remove matches that no longer have robots (can't call them)
                if (!dbMatch.hasRobots()) {
                    indexes.add(index);
                }
            } else {
                indexes.add(index);  
            }
            index++;
        }

        // odstraneni neexistujicich a zapasu bez robotu
        // Remove in reverse order to avoid index shifting issues
        for (int i = indexes.size() - 1; i >= 0; i--) {
            queue.remove((int) indexes.get(i));
        }
    }

    /**
     * Odebere vsechny zapasy, ktery jiz byl odehrane
     * 
     * @return Pocet odstranenych zapasu
     */
    public synchronized int removeAllDone() {
        List<Integer> indexes = new ArrayList<Integer>();
        for (int i = 0; i < this.queue.size(); ++i) {
            if (this.queue.get(i).getState().getName() == EMatchState.DONE) {
                indexes.add(i);
            }
        }
        indexes.stream().forEach((i) -> {
            queue.remove((int) i);
        });
        return indexes.size();
    }

    /**
     * Prida dalsi zapas do fronty. Pokud se v ni jiz nachazi, nebude ho pridavat.
     * 
     * @param match Novy zapas
     * @return True -> zapas byl pridan
     */
    public synchronized boolean add(RobotMatch match) {
        if(match.getState().getName() == EMatchState.DONE) {
            return false;
        }
        if (!this.queue.stream().anyMatch((m) -> (m.getID() == match.getID()))) {
            this.queue.add(match);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Navrati zapas, ktery je jako prvni ve fronte
     * 
     * @return Zapas
     */
    public RobotMatch getFirst() {
        if (this.queue.isEmpty()) {
            return null;
        }
        return this.queue.getFirst();
    }

    /**
     * Zapas s konkretnim ID presune na prvni misto ve fronte
     * 
     * @param matchID ID zapasu
     * @return True -> presunuti bylo uspesne
     */
    public synchronized boolean setFirst(long matchID) {
        if (this.queue.size() < 2) {
            return false;
        }
        for (RobotMatch match : this.queue) {
            if (match.getID() == matchID) {
                this.queue.remove(match);
                this.queue.addFirst(match);
                return true;
            }
        }
        return false;
    }

}

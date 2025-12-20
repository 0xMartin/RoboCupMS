package com.robogames.RoboCupMS.Business.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.robogames.RoboCupMS.Communication;
import com.robogames.RoboCupMS.Business.Enum.ECategory;
import com.robogames.RoboCupMS.Business.Enum.EMatchState;
import com.robogames.RoboCupMS.Business.Object.RobotMatchObj;
import com.robogames.RoboCupMS.Entity.MatchGroup;
import com.robogames.RoboCupMS.Entity.MatchState;
import com.robogames.RoboCupMS.Entity.Playground;
import com.robogames.RoboCupMS.Entity.Robot;
import com.robogames.RoboCupMS.Entity.RobotMatch;
import com.robogames.RoboCupMS.Repository.MatchGroupRepository;
import com.robogames.RoboCupMS.Repository.MatchStateRepository;
import com.robogames.RoboCupMS.Repository.PlaygroundRepository;
import com.robogames.RoboCupMS.Repository.RobotMatchRepository;
import com.robogames.RoboCupMS.Repository.RobotRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Zajistuje spravu zapasu
 */
@Service
public class MatchService {

    @Autowired
    private RobotMatchRepository robotMatchRepository;

    @Autowired
    private MatchStateRepository matchStateRepository;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private PlaygroundRepository playgroundRepository;

    @Autowired
    private MatchGroupRepository matchGroupRepository;

    /**
     * Typy zprav
     */
    public static enum Message {
        CREATE,
        REMOVE,
        REMOVE_ALL,
        WRITE_SCORE,
        REMATCH
    }

    /**
     * Navrati vsechny zapasy
     * 
     * @return Seznam vsech zapasu
     */
    public List<RobotMatch> getAll() {
        List<RobotMatch> all = this.robotMatchRepository.findAll();
        return all;
    }

    /**
     * Navrati vsechny zapasy pro konkretni rocnik
     * 
     * @param year Rocnik souteze
     * @return Seznam vsech zapasu
     */
    public List<RobotMatch> allByYear(int year) {
        Stream<RobotMatch> filter = this.robotMatchRepository.findAll().stream()
                .filter((m) -> (m.getRobot().getTeamRegistration().getCompetitionYear() == year));
        List<RobotMatch> out = new ArrayList<RobotMatch>();
        filter.forEach((m) -> {
            out.add(m);
        });
        return out;
    }

    /**
     * Naplanuje novy zapas
     * 
     * @param robotMatchObj Nové parametry zápasu
     */
    public void create(RobotMatchObj robotMatchObj)
            throws Exception {
        // overi zda robot existuje
        Optional<Robot> robot = this.robotRepository.findById(robotMatchObj.getRobotID());
        if (!robot.isPresent()) {
            throw new Exception(String.format("failure, robot with ID [%d] not exists", robotMatchObj.getRobotID()));
        }

        // overi zda hriste existuje
        Optional<Playground> playground = this.playgroundRepository.findById(robotMatchObj.getPlaygroundID());
        if (!playground.isPresent()) {
            throw new Exception(
                    String.format("failure, playground with ID [%d] not exists", robotMatchObj.getPlaygroundID()));
        }

        // overi zda ma robot povoleno zapasit (registrace byla uspesna => povoluje se
        // pri kontrole pred zacatkem souteze)
        if (!robot.get().getConfirmed()) {
            throw new Exception(
                    String.format("failure, robot with ID [%d] is not confirmed", robotMatchObj.getRobotID()));
        }

        // overi zda jiz nebyl prekrocen maximalni pocet zapasu "pokusu"
        int maxRounds = robot.get().getDiscipline().getMaxRounds();
        if (maxRounds >= 0) {
            if (robot.get().getMatches().size() >= maxRounds) {
                throw new Exception(
                        String.format("failure, robot with ID [%d] exceeded the maximum number of matches",
                                robotMatchObj.getRobotID()));
            }
        }

        // overi zda zapasova skupina existuje, pokud je id skupiny zaporne pak jde o
        // zapas jen jednoho robota (line follower, micromouse, ...)
        MatchGroup group = null;
        if (robotMatchObj.getGroupID() >= 0) {
            Optional<MatchGroup> gOpt = this.matchGroupRepository.findById(robotMatchObj.getGroupID());
            if (!gOpt.isPresent()) {
                throw new Exception(
                        String.format("failure, group with ID [%d] not exists", robotMatchObj.getGroupID()));
            }
            group = gOpt.get();

            // overi zda maji vsichni roboti stejnou kategorii ve skupine
            ECategory mainCategory = robot.get().getTeamRegistration().getCategory();
            List<RobotMatch> matches = group.getMatches();
            for (RobotMatch matche : matches) {
                if (matche.getRobot().getTeamRegistration().getCategory() != mainCategory) {
                    throw new Exception(
                            String.format("failure, the robots in the group are not in the same category",
                                    robotMatchObj.getGroupID()));
                }
            }
        }

        // ziska stav zapasu
        MatchState state = matchStateRepository.findByName(EMatchState.WAITING).get();

        // vytvori zapas a ulozi ho do databaze
        RobotMatch m = new RobotMatch(
                robot.get(),
                group,
                playground.get(),
                state);
        this.robotMatchRepository.save(m);

        // odesle do komunikacniho systemu zpravu
        Communication.getInstance().sendAll(this, MatchService.Message.CREATE);
    }

    /**
     * Odstrani zapas
     * 
     * @param id ID zapasu
     */
    public void remove(long id) throws Exception {
        // overi zda zapas existuje
        if (!this.robotMatchRepository.findById(id).isPresent()) {
            throw new Exception(String.format("failure, match with ID [%d] not exists", id));
        }

        // odstrani zapas
        this.robotMatchRepository.deleteById(id);

        // odesle do komunikacniho systemu zpravu
        Communication.getInstance().sendAll(this, MatchService.Message.REMOVE);
    }

    /**
     * Odstrani vsechny zapasy, ktere nalezi do urcite skupiny
     * 
     * @param groudID ID skupiny, jejiz zapasy maji byt odstraneni
     * @return Pocet odstranenych zapasu
     */
    public int removeAll(long groupID) {
        // najde vsechny zapasy prislusici dane skupine
        Stream<RobotMatch> filter = this.robotMatchRepository.findAll().stream()
                .filter((m) -> (m.getGroupID() == groupID));

        // odstani vsechny nalezene zapasy
        int cnt = 0;
        for (Object m : filter.toArray()) {
            ++cnt;
            this.robotMatchRepository.delete((RobotMatch) m);
        }

        // odesle do komunikacniho systemu zpravu
        Communication.getInstance().sendAll(this, MatchService.Message.REMOVE_ALL);
        return cnt;
    }

    /**
     * Zapise vysledne skore zapasu
     * 
     * @param id    ID zapasu
     * @param score Skore zapasu
     */
    public void writeScore(long id, float score) throws Exception {
        Optional<RobotMatch> m = this.robotMatchRepository.findById(id);
        if (m.isPresent()) {
            // zapise skore zapasu
            m.get().setScore(score);
            // nastavi stav jako odehrany
            MatchState state = matchStateRepository.findByName(EMatchState.DONE).get();
            m.get().setMatchState(state);
            this.robotMatchRepository.save(m.get());

            // odesle do komunikacniho systemu zpravu
            Communication.getInstance().sendAll(this, MatchService.Message.WRITE_SCORE);
        } else {
            throw new Exception(String.format("failure, match with ID [%d] not exists", id));
        }
    }

    /**
     * Vyzada opetovne odegrani zapasu. Pokud jde o skupinovy zapas automaticky
     * tento pozadavek vyzada i u ostatnich zapasu.
     * 
     * @param id ID zapasu
     */
    public void rematch(long id) throws Exception {
        // novy stav zapasu
        MatchState state = this.matchStateRepository.findByName(EMatchState.REMATCH).get();

        // provede zmeni
        Optional<RobotMatch> match = this.robotMatchRepository.findById(id);
        if (match.isPresent()) {
            // vynuluje skore a zmeni stav zapasu
            match.get().setScore(0);
            match.get().setMatchState(state);
            this.robotMatchRepository.save(match.get());

            // pokud jde o skupinovy zapas pak pozadavek uplatni i na ostatni zapasy skupiny
            MatchGroup matchGroup = match.get().getMatchGroup();
            if (matchGroup != null) {
                matchGroup.getMatches().stream().forEach((m) -> {
                    m.setScore(0);
                    m.setMatchState(state);
                    this.robotMatchRepository.save(m);
                });
            }

            // odesle do komunikacniho systemu zpravu
            Communication.getInstance().sendAll(this, MatchService.Message.REMATCH);
        } else {
            throw new Exception(String.format("failure, match with ID [%d] not exists", id));
        }
    }

}
